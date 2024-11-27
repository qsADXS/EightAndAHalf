import librosa
import numpy as np
import pandas as pd
import os
from config.logger_config import setup_logger
logger = setup_logger(__name__)
def extractFeatures(musicId, musicPath='data/music/mp3/',output_dir = 'data/music/'):
    logger.info(f"加载音频文件{musicPath}{musicId}")
    # 加载音频文件
    try:
        y, sr = librosa.load(musicPath + musicId + '.mp3', sr=None)
    except FileNotFoundError:
        logger.error(f"音频文件不存在{musicPath + musicId}")
        logger.error(list_files_in_directory(musicPath))
        return False
    logger.info("加载完成,正在查询重复id")

    # 加载CSV文件
    file_path = 'data/music/musicFeatures.csv'  # 替换为你的CSV文件路径
    df = pd.read_csv(file_path)
    target_id = musicId  # 替换为你想要查询的id
    print(f"id:{type(target_id)}")
    print(f"df['id'].values:{type(df['id'].values)}")
    df['id'] = df['id'].astype(str)
    if target_id in df['id'].values:
        # 3. 如果存在，删除该行
        df = df[df['id'] != target_id]
        # 4. 保存修改后的数据到新的CSV文件（可选）
        df.to_csv(file_path, index=False)
        logger.info(f"ID {target_id} 已被删除，并保存到 musicFeatures.csv")
    else:
        logger.info(f"ID {target_id} 不存在于文件中")
    logger.info("正在提取特征")
    # 提取特征
    rmse = librosa.feature.rms(y=y)
    spectral_centroid = librosa.feature.spectral_centroid(y=y, sr=sr)
    spectral_bandwidth = librosa.feature.spectral_bandwidth(y=y, sr=sr)
    rolloff = librosa.feature.spectral_rolloff(y=y, sr=sr)
    zero_crossing_rate = librosa.feature.zero_crossing_rate(y=y)
    mfcc = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=40)
    chroma_stft = librosa.feature.chroma_stft(y=y, sr=sr)
    tonnetz = librosa.feature.tonnetz(y=y, sr=sr)
    chroma_cqt = librosa.feature.chroma_cqt(y=y, sr=sr)
    spectral_contrast = librosa.feature.spectral_contrast(y=y, sr=sr)

    # 计算均值和标准差
    logger.info("计算均值和标准差")
    features = {
    'id': str(musicId),
    'rmse_mean': np.mean(rmse),
    'rmse_std': np.std(rmse),
    'spectral_centroid_mean': np.mean(spectral_centroid),
    'spectral_centroid_std': np.std(spectral_centroid),
    'spectral_bandwidth_mean': np.mean(spectral_bandwidth),
    'spectral_bandwidth_std': np.std(spectral_bandwidth),
    'rolloff_mean': np.mean(rolloff),
    'rolloff_std': np.std(rolloff),
    'zero_crossing_rate_mean': np.mean(zero_crossing_rate),
    'zero_crossing_rate_std': np.std(zero_crossing_rate),
    }

    for i in range(1, 41):
        features[f'mfcc{i}_mean'] = np.mean(mfcc[i-1])
        features[f'mfcc{i}_std'] = np.std(mfcc[i-1])

    for i in range(1, 13):
        features[f'chroma{i}_mean'] = np.mean(chroma_stft[i-1])
        features[f'chroma{i}_std'] = np.std(chroma_stft[i-1])

    for i in range(1, 7):
        features[f'tonnetz{i}_mean'] = np.mean(tonnetz[i-1])
        features[f'tonnetz{i}_std'] = np.std(tonnetz[i-1])

    features['chroma_cqt_mean'] = np.mean(chroma_cqt)
    features['chroma_cqt_std'] = np.std(chroma_cqt)
    features['spectral_contrast_mean'] = np.mean(spectral_contrast)
    features['spectral_contrast_std'] = np.std(spectral_contrast)

    # 将特征转换为 DataFrame
    df = pd.DataFrame([features])

    # 保存为 CSV 文件
    # df.to_csv('data/music/'+ filename +'.csv', index=False)

    os.makedirs(output_dir, exist_ok=True)
    # df.to_csv(os.path.join(output_dir, 'musicFeatures' + '.csv'), index=False, mode='a',
    #           header=not os.path.exists(os.path.join(output_dir, musicId + '.csv')))
    df.to_csv(os.path.join(output_dir, 'musicFeatures' + '.csv'), index=False, mode='a', header=False)

    logger.info("特征提取完成并保存为 CSV 文件。")
    return True


def list_files_in_directory(directory):
    """
    列出指定目录下的所有文件
    :param directory: 目标目录路径
    :return: 文件列表
    """
    try:
        # 获取目录下的所有文件和文件夹
        all_entries = os.listdir(directory)

        # 过滤出文件（排除文件夹）
        files = [entry for entry in all_entries if os.path.isfile(os.path.join(directory, entry))]

        return files
    except FileNotFoundError:
        logger.error(f"目录 {directory} 不存在")
        return []