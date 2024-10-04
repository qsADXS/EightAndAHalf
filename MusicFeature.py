import librosa
import numpy as np
import pandas as pd

# 定义音频文件路径
path = 'data/music/'
filename = 'music2.mp3'
print(f"加载音频文件{path}{filename}")
# 加载音频文件
y, sr = librosa.load(path + filename, sr=None)
print("加载完成")
print("提取特征")
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
print("计算均值和标准差")
features = {
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
df.to_csv('data/csv/'+ filename +'.csv', index=False)

print("特征提取完成并保存为 CSV 文件。")
