#!/usr/bin/python3
import pandas as pd
import pymysql
from config.mysql_config import DB_CONFIG
from config.logger_config import setup_logger
logger = setup_logger(__name__)

def select(sql, params=None):
    logger.info(f"sql查询")
    try:
        db = pymysql.connect(**DB_CONFIG)
        cursor = db.cursor()
        if params:
            cursor.execute(sql, params)
        else:
            cursor.execute(sql)
        results = cursor.fetchall()
        # data = [str(item[0]) for item in results]
        # return data
        return results
    except pymysql.MySQLError as e:
        logger.error(f"Error: unable to fetch data - {e}")
        return []
    finally:
        # 确保数据库连接在任何情况下都被关闭
        if 'db' in locals() and db.open:
            db.close()

# 最近播放
sql_history = ("select pm.music_id, pm.created_at, up.user_id, pm.listen_count from playlists p "
       "join user_playlists up on p.playlist_id = up.playlist_id "
       "join playlist_music pm on p.playlist_id = pm.playlist_id "
       "and p.playlist_name = '最近播放' "
        "and up.relationship_type = 0;")
# 收藏
sql_favorite = ("select DISTINCT pm.music_id, pm.created_at, up.user_id from playlists p "
       "join user_playlists up on p.playlist_id = up.playlist_id "
       "join playlist_music pm on p.playlist_id = pm.playlist_id "
       "and p.playlist_name != '最近播放' ")

# def get_user_music(history=False):
#     if history:
#         return select(sql)
#     else:
#         return select(sql1)


def get_user_music(history=False):
    if history:
        data = select(sql_history)
        columns = ['song_id', 'created_at', 'user_id', 'listen_count']
        df = pd.DataFrame(data, columns=columns)

        # 将 'created_at' 转换为时间戳（Unix 时间）
        df['timestamp'] = pd.to_datetime(df['created_at'], errors='coerce').astype('int64') // 10**9

        # 选择最终需要的列，重命名为与示例匹配的列名
        play_history = df[['user_id', 'song_id', 'timestamp', 'listen_count']].copy()
        play_history = play_history.rename(columns={'listen_count': 'play_count'})

        return play_history

    else:
        data = select(sql_favorite)
        columns = ['song_id', 'created_at', 'user_id']
        df = pd.DataFrame(data, columns=columns)

        if 'created_at' in df.columns:
            df = df.drop(columns=['created_at'])

        favorite_history = df[['user_id', 'song_id']].copy()
        return favorite_history

def get_latest_play_time(play_history_df):
    latest_play_time_df = play_history_df.groupby('user_id')['timestamp'].max().reset_index()
    latest_play_time_df = latest_play_time_df.rename(columns={'timestamp': 'latest_play_timestamp'})
    return latest_play_time_df

def get_random_music(k):
    sql_random = f"SELECT music_id FROM `EightandHalf-music`.music ORDER BY RAND() LIMIT {k}"
    results = select(sql_random)
    # 将结果转换为列表
    music_ids = [item[0] for item in results]

    return music_ids

if __name__ == "__main__":
    # 获取播放历史
    print("历史记录")
    history_df = get_user_music(history=True)
    print(history_df)

    # 获取收藏列表
    print("收藏列表")
    favorite_df = get_user_music(history=False)
    print(favorite_df)
