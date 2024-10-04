import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score

# 1. 加载数据集
filename = 'music3.mp3.csv'
print("加载数据集train_data_final")
train_data = pd.read_csv('data/archive/train_data_final.csv')
print(f"加载数据集{filename}")
audio_features = pd.read_csv('data/csv/' + filename)

# 2. 数据预处理
# 确保训练数据和音频特征的列一致
print("数据预处理")
common_columns = train_data.columns.difference(['label'])
audio_features = audio_features[common_columns]

# 分离特征和标签
X_train = train_data.drop(columns=['label'])
y_train = train_data['label']

# 3. 训练模型
# 使用随机森林分类器作为示例
print("训练模型")
model = RandomForestClassifier(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

# 4. 预测
# 确保预测时的特征列名与训练时的特征列名一致
audio_features.columns = X_train.columns
predicted_label = model.predict(audio_features)

# 输出预测结果
print(f"Predicted Label: {predicted_label[0]}")
