###
# 本代码仅用于学习，不用作任何其他用途
# ###

import http.client
import json
import os
import time

#https://*/search/qq
###
# curl --location --request GET 'https://ip/search/qq?keyword=抖音热门&page=2&size=30' \
# --header 'User-Agent: Apifox/1.0.0 (https://apifox.com)'
# ###
conn = http.client.HTTPSConnection("ip")
payload = ''
headers = {
   'User-Agent': 'Apifox/1.0.0 (https://apifox.com)',
   'Accept': '*/*',
   'Host': 'ip',
   'Connection': 'keep-alive'
}
for i in range(1):
    print(f"第{i + 1}次：")
    url = "/search/qq?keyword=pink%20Floyd&page=" + str(i+1) +"&size=10"
    conn.request("GET", url, payload, headers)
    res = conn.getresponse()
    data_str = res.read().decode("utf-8")
    print(data_str)
    data = json.loads(data_str)
    song_list = data['result']['list']
    # 打印提取的 list 数据
    for song in song_list:
        file_name = song['id'] + '.json'
        save_path = os.path.join('./music/czh', file_name)
        os.makedirs(os.path.dirname(save_path), exist_ok=True)
        with open(save_path, 'w', encoding='utf-8') as json_file:
            json.dump(song, json_file, ensure_ascii=False, indent=4)

        print(f"JSON数据已保存到: {save_path}")
    print("暂停")
    time.sleep(3)



