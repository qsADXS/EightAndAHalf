import pandas as pd
from model.Sar import SARSingleNode
from config.logger_config import setup_logger
from data.PreprocessMusic import calculate_user_song_ratings
from recommend.ColdStart import similar
from mysql_utils.db_operations import get_random_music

logger = setup_logger(__name__)
# # 用户收藏的歌
#
# favorite_songs = pd.DataFrame({
#     'user': ['user1', 'user2', 'user1', 'user3'],
#     'item': ['song1', 'song2', 'song3', 'song1'],
# })
#
# play_history = pd.DataFrame({
#     'user': ['user1', 'user2', 'user1', 'user3', 'user2'],
#     'item': ['song1', 'song2', 'song3', 'song1', 'song3'],
#     'timestamp': [1, 2, 3, 4, 5],  # 示例时间戳
# })
#
# # 将收藏和播放历史合并
# play_history['rating'] = 1  # 假设播放过的歌曲评分为1
# combined_data = pd.concat([
#     favorite_songs.assign(rating=1),  # 收藏的歌曲评分为1
#     play_history[['user', 'item', 'rating', 'timestamp']]
# ], ignore_index=True)
#
# # 如果没有时间戳，可以设定为当前时间
# combined_data['timestamp'] = combined_data.get('timestamp', pd.Timestamp.now().timestamp())
def sar(userId,k=10):
    data = calculate_user_song_ratings()
    # 初始化模型
    sar_model = SARSingleNode(
        col_user='user',
        col_item='item',
        col_rating='rating',
        col_timestamp='timestamp',
        similarity_type='jaccard',  # 选择相似度类型
        time_decay_coefficient=30,  # 评分衰减系数
        timedecay_formula=False,  # 应用时间衰减
        threshold=1,  # 物品共现次数阈值
        normalize=True,  # 是否归一化
    )
    # 拟合模型
    sar_model.fit(data)
    logger.info("训练完成")
    # 创建测试集
    test_users = pd.DataFrame({
        'user': [userId],
    })
    logger.info("开始获取")
    top_k_recommendations = sar_model.recommend_k_items(test_users, top_k=k, sort_top_k=True, remove_seen=True)
    return top_k_recommendations[['item','prediction']]


def getAdditionalItems(userId, items, k):
    allPredictions = 0.0
    for index,row in items.iterrows():
        allPredictions += row['prediction']
    additional_items = []
    for index,row in items.iterrows():
        additional_items.extend(similar(row['item'],k= round(1.2*k)))
    return additional_items


def getRecommendMusic(userId, k=16):
    try:
        items = sar(userId, k)
        music = items['item'].tolist()
        if len(items) < k:
            # 计算需要补充的 item 数量
            additional_count = k - len(items)
            # 调用 getAdditionalItems 函数获取额外的 item
            additional_items = getAdditionalItems(userId, items, additional_count)
            # 去重并按重复次数排序
            item_counts = {}
            for item in additional_items:
                if item in item_counts:
                    item_counts[item] += 1
                else:
                    item_counts[item] = 1
            # 按重复次数从高到低排序
            sorted_items = sorted(item_counts.items(), key=lambda x: x[1], reverse=True)
            # 过滤掉与 music 列表中相同的元素
            unique_additional_items = [item for item, count in sorted_items if item not in music]
            # 将额外的 item 添加到 items 列表中
            music.extend(unique_additional_items)
        return music[:k]
    except Exception as e:
        return get_random_music(k)
