# 参考https://github.com/recommenders-team/recommenders
import numpy as np
import pandas as pd
from pika.compat import time_now

from config.logger_config import setup_logger
from scipy import sparse

from utils.python_utils import (
    cosine_similarity,
    inclusion_index,
    jaccard,
    lexicographers_mutual_information,
    lift,
    mutual_information,
    exponential_decay,
    get_top_k_scored_items,
    rescale,
)
from utils import constants
"""
1. 初始化模型参数：
    定义用户、物品、评分、时间戳和预测结果的列名。
    选择相似度计算方法（如共现、余弦相似度、Jaccard 相似度等）。
    设置时间衰减系数、阈值和归一化选项。
2. 计算用户-物品亲和力矩阵：
    通过稀疏矩阵表示用户对物品的评分，构建用户-物品亲和力矩阵。
3. 计算时间衰减：
    根据时间衰减系数对评分进行衰减，以反映评分的时效性。
4. 计算物品共现矩阵：
    通过用户-物品矩阵的转置和乘积，计算物品之间的共现矩阵。
5. 生成连续索引：
    为用户和物品生成连续的索引，以减少内存使用。
6. 训练模型：
    收集用户亲和力矩阵，计算物品共现矩阵，并根据选择的相似度类型计算物品相似度矩阵。
7. 评分和推荐：
    为测试用户评分所有物品，并根据评分结果推荐 top K 物品。
    支持移除训练中见过的物品，以确保推荐物品的新颖性。
8. 获取最受欢迎的物品：
    获取所有用户中最频繁出现的 top K 物品。
9. 基于物品的推荐：
    基于提供的种子物品，推荐与之最相似的 top K 物品。
10. 获取最相似的用户：
    基于用户对物品的亲和力，计算与给定用户最相似的用户。
11. 预测评分：
    仅输出测试集中用户-物品对的 SAR 评分。
"""

# 定义相似度类型的常量
SIM_COOCCUR = "cooccurrence"
SIM_COSINE = "cosine"
SIM_INCLUSION_INDEX = "inclusion index"
SIM_JACCARD = "jaccard"
SIM_LEXICOGRAPHERS_MUTUAL_INFORMATION = "lexicographers mutual information"
SIM_LIFT = "lift"
SIM_MUTUAL_INFORMATION = "mutual information"

# 初始化日志记录器
logger = setup_logger(__name__)

class SARSingleNode:
    """Simple Algorithm for Recommendations (SAR) 实现

    SAR 是一种快速、可扩展的自适应算法，用于基于用户交易历史和物品描述的个性化推荐。
    其核心思想是推荐用户已经表现出亲和力的物品。它通过以下步骤实现：
    1) 估计用户对物品的亲和力，2) 估计物品之间的相似度，然后 3) 将这些估计结合起来，为给定用户生成一组推荐。
    """

    def __init__(
        self,
        col_user=constants.DEFAULT_USER_COL,#用户列名
        col_item=constants.DEFAULT_ITEM_COL,#物品列名
        col_rating=constants.DEFAULT_RATING_COL,#评分列名
        col_timestamp=constants.DEFAULT_TIMESTAMP_COL,#时间戳列名
        col_prediction=constants.DEFAULT_PREDICTION_COL,#预测列名
        similarity_type=SIM_JACCARD,#计算物品-物品相似度的选项
        time_decay_coefficient=30,#(float): 评分衰减系数，单位为天
        time_now=None,#(int | None): 当前时间，用于时间衰减计算
        timedecay_formula=False,#(bool): 是否应用时间衰减
        threshold=1,#物品-物品共现次数低于此阈值将被移除
        normalize=False,#是否对预测结果进行归一化
    ):
        """初始化模型参数

        Args:
            col_user (str): 用户列名
            col_item (str): 物品列名
            col_rating (str): 评分列名
            col_timestamp (str): 时间戳列名
            col_prediction (str): 预测列名
            similarity_type (str): 计算物品-物品相似度的选项，可选值为
              ['cooccurrence', 'cosine', 'inclusion index', 'jaccard',
              'lexicographers mutual information', 'lift', 'mutual information']
            time_decay_coefficient (float): 评分衰减系数，单位为天
            time_now (int | None): 当前时间，用于时间衰减计算
            timedecay_formula (bool): 是否应用时间衰减
            threshold (int): 物品-物品共现次数低于此阈值将被移除
            normalize (bool): 是否对预测结果进行归一化
        """
        self.col_rating = col_rating
        self.col_item = col_item
        self.col_user = col_user
        self.col_timestamp = col_timestamp
        self.col_prediction = col_prediction

        # 可用的相似度类型
        available_similarity_types = [
            SIM_COOCCUR,
            SIM_COSINE,
            SIM_INCLUSION_INDEX,
            SIM_JACCARD,
            SIM_LIFT,
            SIM_MUTUAL_INFORMATION,
            SIM_LEXICOGRAPHERS_MUTUAL_INFORMATION,
        ]
        if similarity_type not in available_similarity_types:
            raise ValueError(
                '相似度类型必须是以下之一："'
                + '" | "'.join(available_similarity_types)
                + '"'
            )
        self.similarity_type = similarity_type
        self.time_decay_half_life = (
            time_decay_coefficient * 24 * 60 * 60
        )  # 转换为秒
        self.time_decay_flag = timedecay_formula
        self.time_now = time_now
        self.threshold = threshold
        self.user_affinity = None
        self.item_similarity = None
        self.item_frequencies = None
        self.user_frequencies = None

        # 阈值 - 低于此阈值的物品共现次数将被设置为零
        if self.threshold <= 0:
            raise ValueError("阈值不能小于0")

        # 设置标志以捕获归一化评分用户-物品亲和力矩阵以缩放分数
        self.normalize = normalize
        self.col_unity_rating = "_unity_rating"
        self.unity_user_affinity = None

        # 用于映射用户/物品ID到内部索引的列
        self.col_item_id = "_indexed_items"
        self.col_user_id = "_indexed_users"

        # 获取训练和测试数据中的所有用户和物品
        self.n_users = None
        self.n_items = None

        # 评分的最小值和最大值，从训练数据中获取
        self.rating_min = None
        self.rating_max = None

        # 物品到矩阵元素的映射
        self.user2index = None
        self.item2index = None

        # 上述映射的反向映射 - 映射数组索引到实际字符串ID
        self.index2item = None
        self.index2user = None

    def compute_affinity_matrix(self, df, rating_col):
        """亲和力矩阵。

        用户-物品亲和力矩阵可以通过将用户和物品视为稀疏矩阵中的索引，并将事件视为数据来构建。
        这里，我们将评分视为事件权重。我们通过在不同的稀疏矩阵格式之间转换来去重用户-物品对，否则它们将被相加。

        Args:
            df (pandas.DataFrame): 包含用户和物品索引的DataFrame
            rating_col (str): 用于评分的列名

        Returns:
            sparse.csr: 亲和力矩阵，以压缩稀疏行（CSR）格式返回。
        """

        return sparse.coo_matrix(
            (df[rating_col], (df[self.col_user_id], df[self.col_item_id])),
            shape=(self.n_users, self.n_items),
        ).tocsr()

    def compute_time_decay(self, df, decay_column):
        """计算时间衰减。

        Args:
            df (pandas.DataFrame): 包含用户和物品的DataFrame
            decay_column (str): 需要衰减的列

        Returns:
            pandas.DataFrame: 包含衰减列的DataFrame
        """

        # 如果time_now为None，则使用最新时间
        if self.time_now is None:
            self.time_now = df[self.col_timestamp].max()
        # 对每个评分应用时间衰减
        df[decay_column] *= exponential_decay(
            value=df[self.col_timestamp],
            max_val=self.time_now,
            half_life=self.time_decay_half_life,
        )

        # 按用户-物品分组并求和，作为用户-物品亲和力
        return df.groupby([self.col_user, self.col_item]).sum().reset_index()

    def compute_cooccurrence_matrix(self, df):
        """共现矩阵。

        共现矩阵定义为 :math:`C = U^T * U`
        其中 U 是用户亲和力矩阵，值为1（而不是评分）。

        Args:
            df (pandas.DataFrame): 包含用户和物品的DataFrame

        Returns:
            numpy.ndarray: 共现矩阵
        """
        user_item_hits = sparse.coo_matrix(
            (np.repeat(1, df.shape[0]), (df[self.col_user_id], df[self.col_item_id])),
            shape=(self.n_users, self.n_items),
        ).tocsr()

        item_cooccurrence = user_item_hits.transpose().dot(user_item_hits)
        item_cooccurrence = item_cooccurrence.multiply(
            item_cooccurrence >= self.threshold
        )

        return item_cooccurrence.astype(df[self.col_rating].dtype)

    def set_index(self, df):
        """生成用户和物品的连续索引以减少内存使用。

        Args:
            df (pandas.DataFrame): 包含用户和物品ID的DataFrame
        """

        # 生成物品到连续索引值的映射
        self.index2item = dict(enumerate(df[self.col_item].unique()))
        self.index2user = dict(enumerate(df[self.col_user].unique()))

        # 反转上述映射
        self.item2index = {v: k for k, v in self.index2item.items()}
        self.user2index = {v: k for k, v in self.index2user.items()}

        # 设置用户和物品的总数
        self.n_users = len(self.user2index)
        self.n_items = len(self.index2item)

    def fit(self, df):
        """SAR的主要拟合方法。

        注意：请确保`df`没有重复项。

        Args:
            df (pandas.DataFrame): 用户物品评分DataFrame（无重复项）。
        """
        select_columns = [self.col_user, self.col_item, self.col_rating]
        if self.time_decay_flag:
            select_columns += [self.col_timestamp]

        if df[select_columns].duplicated().any():
            raise ValueError("DataFrame中不应有重复项")

        # 如果尚未生成连续索引，则生成
        if self.index2item is None:
            self.set_index(df)

        logger.info("收集用户亲和力矩阵")
        if not np.issubdtype(df[self.col_rating].dtype, np.number):
            raise TypeError("评分列数据类型必须是数值型")

        # 复制DataFrame以避免修改输入
        temp_df = df[select_columns].copy()

        if self.time_decay_flag:
            logger.info("计算时间衰减亲和力")
            temp_df = self.compute_time_decay(df=temp_df, decay_column=self.col_rating)
        logger.info("创建索引列")
        # 添加用户和物品ID到索引的映射
        temp_df.loc[:, self.col_item_id] = temp_df[self.col_item].apply(
            lambda item: self.item2index.get(item, np.NaN)
        )
        temp_df.loc[:, self.col_user_id] = temp_df[self.col_user].apply(
            lambda user: self.user2index.get(user, np.NaN)
        )

        if self.normalize:
            self.rating_min = temp_df[self.col_rating].min()
            self.rating_max = temp_df[self.col_rating].max()
            logger.info("计算归一化因子")
            temp_df[self.col_unity_rating] = 1.0
            if self.time_decay_flag:
                temp_df = self.compute_time_decay(
                    df=temp_df, decay_column=self.col_unity_rating
                )
            self.unity_user_affinity = self.compute_affinity_matrix(
                df=temp_df, rating_col=self.col_unity_rating
            )

        # 亲和力矩阵
        logger.info("构建用户亲和力稀疏矩阵")
        self.user_affinity = self.compute_affinity_matrix(
            df=temp_df, rating_col=self.col_rating
        )

        # 计算物品共现
        logger.info("计算物品共现")
        item_cooccurrence = self.compute_cooccurrence_matrix(df=temp_df)

        # 释放一些空间
        del temp_df

        # 生成每个唯一物品的频率数组
        self.item_frequencies = item_cooccurrence.diagonal()

        logger.info("计算物品相似度")
        if self.similarity_type == SIM_COOCCUR:
            logger.info("使用共现相似度")
            self.item_similarity = item_cooccurrence
        elif self.similarity_type == SIM_COSINE:
            logger.info("使用余弦相似度")
            self.item_similarity = cosine_similarity(item_cooccurrence)
        elif self.similarity_type == SIM_INCLUSION_INDEX:
            logger.info("使用包含指数")
            self.item_similarity = inclusion_index(item_cooccurrence)
        elif self.similarity_type == SIM_JACCARD:
            logger.info("使用Jaccard相似度")
            self.item_similarity = jaccard(item_cooccurrence)
        elif self.similarity_type == SIM_LEXICOGRAPHERS_MUTUAL_INFORMATION:
            logger.info("使用词典学家互信息相似度")
            self.item_similarity = lexicographers_mutual_information(item_cooccurrence)
        elif self.similarity_type == SIM_LIFT:
            logger.info("使用提升相似度")
            self.item_similarity = lift(item_cooccurrence)
        elif self.similarity_type == SIM_MUTUAL_INFORMATION:
            logger.info("使用互信息相似度")
            self.item_similarity = mutual_information(item_cooccurrence)
        else:
            raise ValueError("未知的相似度类型: {}".format(self.similarity_type))

        # 释放一些空间
        del item_cooccurrence

        logger.info("训练完成")

    def score(self, test, remove_seen=False):
        """为测试用户评分所有物品。

        Args:
            test (pandas.DataFrame): 测试用户
            remove_seen (bool): 标志，用于从推荐中移除训练中见过的物品

        Returns:
            numpy.ndarray: 用户对所有物品的兴趣值。
        """

        # 从测试集中获取用户/物品索引
        user_ids = list(
            map(
                lambda user: self.user2index.get(user, np.NaN),
                test[self.col_user].unique(),
            )
        )
        if any(np.isnan(user_ids)):
            raise ValueError("SAR无法评分训练集中不存在的用户")

        # 通过矩阵乘法计算原始分数
        logger.info("计算推荐分数")
        test_scores = self.user_affinity[user_ids, :].dot(self.item_similarity)

        # 确保我们使用的是密集的ndarray
        if isinstance(test_scores, sparse.spmatrix):
            test_scores = test_scores.toarray()

        if self.normalize:
            counts = self.unity_user_affinity[user_ids, :].dot(self.item_similarity)
            user_min_scores = (
                np.tile(counts.min(axis=1)[:, np.newaxis], test_scores.shape[1])
                * self.rating_min
            )
            user_max_scores = (
                np.tile(counts.max(axis=1)[:, np.newaxis], test_scores.shape[1])
                * self.rating_max
            )
            test_scores = rescale(
                test_scores,
                self.rating_min,
                self.rating_max,
                user_min_scores,
                user_max_scores,
            )

        # 移除训练集中的物品，以确保推荐物品总是新颖的
        if remove_seen:
            logger.info("移除见过的物品")
            test_scores += self.user_affinity[user_ids, :] * -np.inf

        return test_scores

    def get_popularity_based_topk(self, top_k=10, sort_top_k=True, items=True):
        """获取所有用户中最频繁出现的top K物品。

        Args:
            top_k (int): 推荐的top物品数量
            sort_top_k (bool): 是否对top k结果进行排序
            items (bool): 如果为false，返回最频繁的用户

        Returns:
            pandas.DataFrame: top k最受欢迎的物品。
        """
        if items:
            frequencies = self.item_frequencies
            col = self.col_item
            idx = self.index2item
        else:
            if self.user_frequencies is None:
                self.user_frequencies = self.user_affinity.getnnz(axis=1).astype(
                    "int64"
                )
            frequencies = self.user_frequencies
            col = self.col_user
            idx = self.index2user

        test_scores = np.array([frequencies])

        logger.info("获取top K")
        top_components, top_scores = get_top_k_scored_items(
            scores=test_scores, top_k=top_k, sort_top_k=sort_top_k
        )

        return pd.DataFrame(
            {
                col: [idx[item] for item in top_components.flatten()],
                self.col_prediction: top_scores.flatten(),
            }
        )

    def get_item_based_topk(self, items, top_k=10, sort_top_k=True):
        """基于提供的种子物品，获取最相似的top K物品。
        此方法将获取一组物品，并根据训练期间定义的相似度矩阵推荐最相似的物品。
        这允许对冷用户（训练期间未见过的用户）进行推荐，注意 - 模型不会更新。

        以下选项基于items输入中的信息：
        1. 单个用户或种子物品：仅包含物品列（评分假设为1）
        2. 单个用户或种子物品，带评分：包含物品列和评分列
        3. 多个用户或种子物品：包含物品和用户列（用户ID仅用于分隔物品集）
        4. 多个用户或种子物品，带评分：包含物品、用户和评分列

        Args:
            items (pandas.DataFrame): 包含物品、用户（可选）和评分（可选）列的DataFrame
            top_k (int): 推荐的top物品数量
            sort_top_k (bool): 是否对top k结果进行排序

        Returns:
            pandas.DataFrame: 排序后的top k推荐物品
        """

        # 将物品ID转换为索引
        item_ids = np.asarray(
            list(
                map(
                    lambda item: self.item2index.get(item, np.NaN),
                    items[self.col_item].values,
                )
            )
        )

        # 如果没有提供评分，则假设所有评分为1
        if self.col_rating in items.columns:
            ratings = items[self.col_rating]
        else:
            ratings = pd.Series(np.ones_like(item_ids))

        # 创建用户ID的本地映射
        if self.col_user in items.columns:
            test_users = items[self.col_user]
            user2index = {x[1]: x[0] for x in enumerate(items[self.col_user].unique())}
            user_ids = test_users.map(user2index)
        else:
            # 如果没有用户列，则假设所有条目都属于单个用户
            test_users = pd.Series(np.zeros_like(item_ids))
            user_ids = test_users
        n_users = user_ids.drop_duplicates().shape[0]

        # 使用种子物品生成伪用户亲和力
        pseudo_affinity = sparse.coo_matrix(
            (ratings, (user_ids, item_ids)), shape=(n_users, self.n_items)
        ).tocsr()

        # 通过矩阵乘法计算原始分数
        test_scores = pseudo_affinity.dot(self.item_similarity)

        # 移除种子集中的物品，以确保推荐物品是新颖的
        test_scores[user_ids, item_ids] = -np.inf

        top_items, top_scores = get_top_k_scored_items(
            scores=test_scores, top_k=top_k, sort_top_k=sort_top_k
        )

        df = pd.DataFrame(
            {
                self.col_user: np.repeat(
                    test_users.drop_duplicates().values, top_items.shape[1]
                ),
                self.col_item: [self.index2item[item] for item in top_items.flatten()],
                self.col_prediction: top_scores.flatten(),
            }
        )

        # 删除无效物品
        return df.replace(-np.inf, np.nan).dropna()

    def get_topk_most_similar_users(self, user, top_k, sort_top_k=True):
        """基于用户对物品的亲和力，计算与给定用户最相似的用户。

        Args:
            user (int): 要获取最相似用户的用户
            top_k (int): 推荐的top用户数量
            sort_top_k (bool): 是否对top k结果进行排序

        Returns:
            pandas.DataFrame: top k最相似的用户及其分数
        """
        user_idx = self.user2index[user]
        similarities = self.user_affinity[user_idx].dot(self.user_affinity.T).toarray()
        similarities[0, user_idx] = -np.inf

        top_items, top_scores = get_top_k_scored_items(
            scores=similarities, top_k=top_k, sort_top_k=sort_top_k
        )

        df = pd.DataFrame(
            {
                self.col_user: [self.index2user[user] for user in top_items.flatten()],
                self.col_prediction: top_scores.flatten(),
            }
        )

        # 删除无效物品
        return df.replace(-np.inf, np.nan).dropna()

    def recommend_k_items(self, test, top_k=10, sort_top_k=True, remove_seen=False):
        """为测试集中的所有用户推荐top K物品

        Args:
            test (pandas.DataFrame): 测试用户
            top_k (int): 推荐的top物品数量
            sort_top_k (bool): 是否对top k结果进行排序
            remove_seen (bool): 标志，用于从推荐中移除训练中见过的物品

        Returns:
            pandas.DataFrame: 每个用户的top k推荐物品
        """

        test_scores = self.score(test, remove_seen=remove_seen)

        top_items, top_scores = get_top_k_scored_items(
            scores=test_scores, top_k=top_k, sort_top_k=sort_top_k
        )

        df = pd.DataFrame(
            {
                self.col_user: np.repeat(
                    test[self.col_user].drop_duplicates().values, top_items.shape[1]
                ),
                self.col_item: [self.index2item[item] for item in top_items.flatten()],
                self.col_prediction: top_scores.flatten(),
            }
        )

        # 删除无效物品
        return df.replace(-np.inf, np.nan).dropna()

    def predict(self, test):
        """仅输出测试集中用户-物品对的SAR评分

        Args:
            test (pandas.DataFrame): 包含用户和物品的测试DataFrame

        Returns:
            pandas.DataFrame: 包含预测结果的DataFrame
        """

        test_scores = self.score(test)
        user_ids = np.asarray(
            list(
                map(
                    lambda user: self.user2index.get(user, np.NaN),
                    test[self.col_user].values,
                )
            )
        )

        # 创建新物品到零的映射
        item_ids = np.asarray(
            list(
                map(
                    lambda item: self.item2index.get(item, np.NaN),
                    test[self.col_item].values,
                )
            )
        )
        nans = np.isnan(item_ids)
        if any(nans):
            logger.warning(
                "测试中发现的物品在训练期间未见过，新物品的评分为0"
            )
            test_scores = np.append(test_scores, np.zeros((self.n_users, 1)), axis=1)
            item_ids[nans] = self.n_items
            item_ids = item_ids.astype("int64")

        df = pd.DataFrame(
            {
                self.col_user: test[self.col_user].values,
                self.col_item: test[self.col_item].values,
                self.col_prediction: test_scores[user_ids, item_ids],
            }
        )
        return df