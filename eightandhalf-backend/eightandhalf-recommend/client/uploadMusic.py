import requests

def upload(musicName,musicPath,coverPath,singer,album):
   url = "http://localhost:8080/music/upload"

   payload = {
      'musicName': musicName,
      'musicCategory': 'Rock',
      'singer': str(singer),
      'album': str(album)
   }
   files=[
      ('musicFile',(musicName+'.mp3',open(musicPath,'rb'),'application/octet-stream')),
      ('coverFile',(musicName+'.jpg',open(coverPath,'rb'),'application/octet-stream'))
   ]
   headers = {
      'Access-Token': '',
      'Refresh-Token': '',
      'User-Agent': 'Apifox/1.0.0 (https://apifox.com)'
   }

   response = requests.request("POST", url, headers=headers, data=payload, files=files)

   print(response.text)
   print("=====")