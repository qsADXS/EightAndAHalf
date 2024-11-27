import http.client
import json

# https://*//url/qq/{id}/{quality}
###
# curl --location --request GET 'https://ip//url/qq//' \
# --header 'unlockcode: B62F' \
# --header 'User-Agent: Apifox/1.0.0 (https://apifox.com)'
# ###
import http.client
import json
import random
from config.logger_config import setup_logger
logger = setup_logger(__name__)
import requests
import os
from client.uploadMusic import upload
type = 'czh'
def download_file(url, local_filename):
   logger.info("Downloading file {}".format(local_filename))
   # 发送GET请求
   response = requests.get(url, stream=True)

   # 检查请求是否成功
   if response.status_code == 200:
      # 打开文件并写入内容
      with open(local_filename, 'wb') as f:
         for chunk in response.iter_content(chunk_size=8192):
            f.write(chunk)
      print(f'文件已下载并保存为 {local_filename}')
   else:
      logger.error(f'下载失败，状态码: {response.status_code}')
def downloadMusic(id,picUrl):
   conn = http.client.HTTPSConnection("ip")
   payload = ''
   headers = {
      'unlockcode': 'F1AC',
      'User-Agent': 'Chrome/129.0.0.0'
   }
   conn.request("GET", "/url/qq/"+id+"/128", payload, headers)
   res = conn.getresponse()
   data = json.loads(res.read())
   print(data)
   if data.get('success'):
      url = data.get('message')
      filename = '/home/zephyrsky/study/porgrame/EAH/eightandhalf-backend/eightandhalf-recommend/client/music/'+ type +'/'+id+'.mp3'
      picfilename = '/home/zephyrsky/study/porgrame/EAH/eightandhalf-backend/eightandhalf-recommend/client/pic/'+ type +'/'+id+'.jpg'
      download_file(url,filename)
      download_file(picUrl,picfilename)

import time
folder_path = ('./music/' + type)

# 遍历文件夹中的所有文件
for index, filename in enumerate(os.listdir(folder_path)):
   if filename.endswith('.json'):  # 只处理JSON文件
      file_path = os.path.join(folder_path, filename)
      logger.info(f'第：{index + 1}次，处理文件：{filename}')
      # 打开并读取JSON文件
      with open(file_path, 'r', encoding='utf-8') as file:
         data = json.load(file)
         id = data.get('id')
         picUrl = data.get('picUrl')
         if picUrl == "":
            picUrl = 'https://s2.loli.net/2024/11/11/Qct2H9mTrd7AsCn.jpg'
         name = data.get('name')
         singer = ', '.join(data.get('singers'))
         album =data.get('albumName')
         downloadMusic(id,picUrl)
         filename = '/home/zephyrsky/study/porgrame/EAH/eightandhalf-backend/eightandhalf-recommend/client/music/'+ type +'/' + id + '.mp3'
         picfilename = '/home/zephyrsky/study/porgrame/EAH/eightandhalf-backend/eightandhalf-recommend/client/pic/'+ type +'/' + id + '.jpg'
         upload(name, filename,picfilename,singer,album)
   time.sleep(random.randint(0,2))