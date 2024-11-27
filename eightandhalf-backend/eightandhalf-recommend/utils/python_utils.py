# Copyright (c) Recommenders contributors.
# Licensed under the MIT License.

import numpy as np
from scipy import sparse
from config.logger_config import setup_logger

logger = setup_logger(__name__)  # 设置日志记录器

def exponential_decay(value, max_val, half_life):
    """计算基于指数衰减的衰减因子。

    大于 `max_val` 的值将被设置为 1。

    参数:
        value (numeric): 需要计算衰减因子的值
        max_val (numeric): 衰减因子为 1 时的值
        half_life (numeric): 衰减因子为 0.5 时的值

    返回:
        float: 衰减因子
    """
    return np.minimum(1.0, np.power(0.5, (max_val - value) / half_life))

def _get_row_and_column_matrix(array):
    """从数组中获取行矩阵和列矩阵的辅助方法。

    参数:
        array (numpy.ndarray): 从中获取行和列矩阵的数组

    返回:
        (numpy.ndarray, numpy.ndarray): (行矩阵, 列矩阵)
    """
    row_matrix = np.expand_dims(array, axis=0)
    column_matrix = np.expand_dims(array, axis=1)
    return row_matrix, column_matrix

def jaccard(cooccurrence):
    """计算共现矩阵的 Jaccard 相似度的辅助方法。

    在比较 Jaccard 与计数共现和提升相似度时，计数倾向于可预测性，即最受欢迎的物品将被推荐大多数时间。提升则倾向于可发现性/意外性，即一个总体上不太受欢迎但被一小部分用户高度偏爱的物品更有可能被推荐。Jaccard 是两者的折中。

    参数:
        cooccurrence (numpy.ndarray): 物品的共现对称矩阵

    返回:
        numpy.ndarray: 任意两个物品之间的 Jaccard 相似度矩阵
    """
    diag_rows, diag_cols = _get_row_and_column_matrix(cooccurrence.diagonal())
    with np.errstate(invalid="ignore", divide="ignore"):
        result = cooccurrence / (diag_rows + diag_cols - cooccurrence)
    return np.array(result) if isinstance(result, np.ndarray) else result.toarray()

def lift(cooccurrence):
    """计算共现矩阵的提升度的辅助方法。

    与基本的共现和 Jaccard 相似度相比，提升倾向于可发现性和意外性，而不是共现倾向于最受欢迎的物品，Jaccard 是两者的折中。

    参数:
        cooccurrence (numpy.ndarray): 物品的共现对称矩阵

    返回:
        numpy.ndarray: 任意两个物品之间的提升度矩阵
    """
    diag_rows, diag_cols = _get_row_and_column_matrix(cooccurrence.diagonal())
    with np.errstate(invalid="ignore", divide="ignore"):
        result = cooccurrence / (diag_rows * diag_cols)
    return np.array(result) if isinstance(result, np.ndarray) else result.toarray()

def mutual_information(cooccurrence):
    """计算共现矩阵的互信息的辅助方法。

    互信息是第 i 个和第 j 个物品列向量所解释的信息量。

    参数:
        cooccurrence (numpy.ndarray): 物品的共现对称矩阵

    返回:
        numpy.ndarray: 任意两个物品之间的互信息矩阵
    """
    with np.errstate(invalid="ignore", divide="ignore"):
        result = np.log2(cooccurrence.shape[0] * lift(cooccurrence))
    return np.array(result) if isinstance(result, np.ndarray) else result.toarray()

def lexicographers_mutual_information(cooccurrence):
    """计算共现矩阵的词典编纂者互信息的辅助方法。

    由于互信息对低频物品有偏差，词典编纂者互信息通过乘以共现频率来修正公式。

    参数:
        cooccurrence (numpy.ndarray): 物品的共现对称矩阵

    返回:
        numpy.ndarray: 任意两个物品之间的词典编纂者互信息矩阵
    """
    with np.errstate(invalid="ignore", divide="ignore"):
        result = cooccurrence * mutual_information(cooccurrence)
    return np.array(result) if isinstance(result, np.ndarray) else result.toarray()

def cosine_similarity(cooccurrence):
    """计算共现矩阵的余弦相似度的辅助方法。

    余弦相似度可以解释为第 i 个和第 j 个物品之间的角度。

    参数:
        cooccurrence (numpy.ndarray): 物品的共现对称矩阵

    返回:
        numpy.ndarray: 任意两个物品之间的余弦相似度矩阵
    """
    diag_rows, diag_cols = _get_row_and_column_matrix(cooccurrence.diagonal())
    with np.errstate(invalid="ignore", divide="ignore"):
        result = cooccurrence / np.sqrt(diag_rows * diag_cols)
    return np.array(result) if isinstance(result, np.ndarray) else result.toarray()

def inclusion_index(cooccurrence):
    """计算共现矩阵的包含指数的辅助方法。

    包含指数衡量物品之间的重叠。

    参数:
        cooccurrence (numpy.ndarray): 物品的共现对称矩阵

    返回:
        numpy.ndarray: 任意两个物品之间的包含指数矩阵
    """
    diag_rows, diag_cols = _get_row_and_column_matrix(cooccurrence.diagonal())
    with np.errstate(invalid="ignore", divide="ignore"):
        result = cooccurrence / np.minimum(diag_rows, diag_cols)
    return np.array(result) if isinstance(result, np.ndarray) else result.toarray()

def get_top_k_scored_items(scores, top_k, sort_top_k=False):
    """从每个用户-物品对的得分矩阵中提取前 K 个物品，可选地按用户排序结果。

    参数:
        scores (numpy.ndarray): 得分矩阵 (用户 x 物品)
        top_k (int): 推荐的物品数量
        sort_top_k (bool): 是否对前 k 个结果进行排序

    返回:
        numpy.ndarray, numpy.ndarray:
        - 得分矩阵中每个用户的前 k 个物品的索引
        - 对应的前 k 个物品的得分
    """
    if isinstance(scores, sparse.spmatrix):
        scores = scores.todense()  # 确保我们处理的是密集的 ndarray

    if scores.shape[1] < top_k:
        logger.warning("物品数量少于 top_k，将 top_k 限制为物品数量")
    k = min(top_k, scores.shape[1])

    test_user_idx = np.arange(scores.shape[0])[:, None]

    # 获取前 K 个物品和得分
    top_items = np.argpartition(scores, -k, axis=1)[:, -k:]
    top_scores = scores[test_user_idx, top_items]

    if sort_top_k:
        sort_ind = np.argsort(-top_scores)
        top_items = top_items[test_user_idx, sort_ind]
        top_scores = top_scores[test_user_idx, sort_ind]

    return np.array(top_items), np.array(top_scores)

def binarize(a, threshold):
    """二值化值。

    参数:
        a (numpy.ndarray): 需要二值化的输入数组
        threshold (float): 阈值，低于该阈值的所有值设置为 0，否则为 1

    返回:
        numpy.ndarray: 二值化后的数组
    """
    return np.where(a > threshold, 1.0, 0.0)

def rescale(data, new_min=0, new_max=1, data_min=None, data_max=None):
    """将数据重新缩放到 `[new_min, new_max]` 范围内。

    如果提供了 data_min 和 data_max，它们将被用作旧的最小值/最大值，而不是从数据中推断。

    注意:
        这与 `scipy.MinMaxScaler` 相同，只是我们可以覆盖旧比例的最小值/最大值。

    参数:
        data (numpy.ndarray): 1d 得分向量或 2d 得分矩阵 (用户 x 物品)
        new_min (int|float): 重新缩放后的数据的最小值
        new_max (int|float): 重新缩放后的数据的最大值
        data_min (None|number): 传递数据的最小值 [如果省略将推断]
        data_max (None|number): 传递数据的最大值 [如果省略将推断]

    返回:
        numpy.ndarray: 重新缩放/归一化后的数据
    """
    data_min = data.min() if data_min is None else data_min
    data_max = data.max() if data_max is None else data_max

    if np.all(data_max == data_min):
        return np.full_like(data, (new_min + new_max) / 2)

    return (data - data_min) / (data_max - data_min) * (new_max - new_min) + new_min
