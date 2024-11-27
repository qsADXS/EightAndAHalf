# Copyright (c) Recommenders contributors. All rights reserved.
# Licensed under the MIT License.

# 默认列名
DEFAULT_USER_COL = "userID"  # 用户ID列的默认名称
DEFAULT_ITEM_COL = "itemID"  # 物品ID列的默认名称
DEFAULT_RATING_COL = "rating"  # 评分列的默认名称
DEFAULT_LABEL_COL = "label"  # 标签列的默认名称
DEFAULT_TITLE_COL = "title"  # 标题列的默认名称
DEFAULT_GENRE_COL = "genre"  # 类别列的默认名称
DEFAULT_RELEVANCE_COL = "relevance"  # 相关性列的默认名称
DEFAULT_TIMESTAMP_COL = "timestamp"  # 时间戳列的默认名称
DEFAULT_PREDICTION_COL = "prediction"  # 预测列的默认名称
DEFAULT_SIMILARITY_COL = "sim"  # 相似度列的默认名称
DEFAULT_ITEM_FEATURES_COL = "features"  # 物品特征列的默认名称
DEFAULT_ITEM_SIM_MEASURE = "item_cooccurrence_count"  # 物品相似度度量列的默认名称

DEFAULT_HEADER = (
    DEFAULT_USER_COL,
    DEFAULT_ITEM_COL,
    DEFAULT_RATING_COL,
    DEFAULT_TIMESTAMP_COL,
)  # 默认的表头列名

COL_DICT = {
    "col_user": DEFAULT_USER_COL,  # 用户列名
    "col_item": DEFAULT_ITEM_COL,  # 物品列名
    "col_rating": DEFAULT_RATING_COL,  # 评分列名
    "col_prediction": DEFAULT_PREDICTION_COL,  # 预测列名
}

# 过滤变量
DEFAULT_K = 10  # 默认的K值，通常用于Top-K推荐
DEFAULT_THRESHOLD = 10  # 默认的阈值，用于过滤数据

# 其他
SEED = 42  # 随机种子，用于确保结果的可重复性
