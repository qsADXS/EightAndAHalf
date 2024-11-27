import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity, euclidean_distances
from scipy.spatial.distance import correlation
import numpy as np


def similar(id, similarity_type='cosine', k=5):
    # 当前目录为测试目录，实际运行应修改为'./data/music/musicFeatures.csv'
    data = pd.read_csv('./data/music/musicFeatures.csv')
    target_item = data[data['id'] == id]
    if target_item.empty:
        raise ValueError(f"ID {id} not found in the dataset.")
    features = data.drop(columns=['id'])
    target_features = target_features = target_item.drop(columns=['id'])

    if similarity_type == 'cosine':
        similarities = cosine_similarity(target_features, features)
    elif similarity_type == 'euclidean':
        # 欧几里得距离越小，相似度越高
        distances = euclidean_distances(target_features, features)
        similarities = 1 / (1 + distances)
    elif similarity_type == 'pearson':
        # 皮尔逊相关系数
        similarities = np.array(
            [correlation(target_features.iloc[0], features.iloc[i]) for i in range(features.shape[0])])
        similarities = similarities.reshape(1, -1)
    else:
        raise ValueError(f"Unsupported similarity type: {similarity_type}")

    similarity_df = pd.DataFrame(similarities.T, index=data['id'], columns=['similarity'])
    similarity_df = similarity_df[similarity_df.index != id]
    top_k_similar = similarity_df.sort_values(by='similarity', ascending=False).head(k)
    return top_k_similar.index.tolist()
    

if __name__ == '__main__':
    # 示例调用
    result = similar(337774636236410880, similarity_type='cosine', k=5)
    print(result)
    result = similar(337774636236410880, similarity_type='euclidean', k=5)
    print(result)
    result = similar(337774636236410880, similarity_type='pearson', k=5)
    print(result)