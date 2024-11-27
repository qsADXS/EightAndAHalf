import pandas as pd
import numpy as np
from mysql_utils.db_operations import get_user_music, get_latest_play_time
from utils.python_utils import exponential_decay, rescale

def calculate_user_song_ratings(w1=0.7, w2=0.3, half_life_days=7):
    """
    计算用户-歌曲评分，结合播放历史和收藏历史数据。

    参数：
        w1 (float): 播放次数的权重，默认值为 0.7
        w2 (float): 收藏行为的权重，默认值为 0.3
        half_life_days (int): 时间衰减的半衰期（单位：天），默认值为 7 天

    返回：
        pandas.DataFrame: 包含用户、歌曲及其评分的数据框，格式为 ['user', 'item', 'rating']
    """
    # 获取用户播放历史
    play_history = get_user_music(history=True)

    # 获取有播放历史的用户列表
    users_with_playback = play_history['user_id'].unique()

    # 获取这些用户的最新播放记录时间
    latest_play_time = get_latest_play_time(play_history)

    # 获取收藏历史，只保留有播放历史的用户
    favorite_history = get_user_music(history=False)
    favorite_history = favorite_history[favorite_history['user_id'].isin(users_with_playback)]

    # 合并播放和收藏数据，左连接保留播放历史中的所有记录
    combined = pd.merge(play_history, favorite_history, on=['user_id', 'song_id'], how='left', indicator=True)

    # 将每个用户的最新播放记录时间合并进去
    combined = pd.merge(combined, latest_play_time, on='user_id', how='left')

    # 半衰期转换为秒
    half_life = half_life_days * 24 * 60 * 60

    # 确保时间戳为数值类型
    combined['timestamp'] = pd.to_numeric(combined['timestamp'], errors='coerce')
    combined['latest_play_timestamp'] = pd.to_numeric(combined['latest_play_timestamp'], errors='coerce')

    # 计算时间衰减因子
    combined['decay_factor'] = combined.apply(
        lambda row: exponential_decay(row['timestamp'], row['latest_play_timestamp'], half_life)
        if pd.notna(row['timestamp']) and pd.notna(row['latest_play_timestamp'])
        else 1,
        axis=1
    )

    # 标准化播放次数
    def normalize_play_count(group):
        play_sum = group['play_count'].sum()
        if play_sum == 0:
            group['normalized_play_count'] = 0
        else:
            group['normalized_play_count'] = group['play_count'] / play_sum
        return group

    combined = combined.groupby('user_id').apply(normalize_play_count)

    # 计算加权播放评分
    combined['weighted_play_rating'] = combined['normalized_play_count'] * combined['decay_factor']
    combined['weighted_play_rating'] = combined['weighted_play_rating'].fillna(0)

    # 收藏行为评分，未收藏的填充为 0
    combined['favorite'] = combined['_merge'].apply(lambda x: 1 if x == 'both' else 0)
    combined['favorite'] = combined['favorite'].fillna(0)

    # 计算最终评分
    combined['rating'] = w1 * combined['weighted_play_rating'] + w2 * combined['favorite']
    combined['rating'] = combined['rating'].fillna(0)

    # 将评分缩放到 [1, 5] 范围
    combined['scaled_rating'] = rescale(combined['rating'].values, new_min=1, new_max=5)

    # 选择需要的列，并重命名
    final_ratings = combined[['user_id', 'song_id', 'scaled_rating']].copy()
    final_ratings.rename(columns={'user_id': 'user', 'song_id': 'item', 'scaled_rating': 'rating'}, inplace=True)

    return final_ratings

def main():
    # 计算用户-歌曲评分
    ratings = calculate_user_song_ratings()

    # 查看评分结果
    print("用户-歌曲评分：")
    print(ratings)

if __name__ == "__main__":
    main()




