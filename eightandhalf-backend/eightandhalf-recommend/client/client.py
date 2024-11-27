import grpc

from proto import recommend_pb2
from proto import recommend_pb2_grpc
from config.logger_config import setup_logger
logger = setup_logger(__name__)

def generate_chunks(filename, musicId, chunk_size=1024*1024):
    with open(filename, 'rb') as f:
        while True:
            logger.info("上传文件")
            chunk = f.read(chunk_size)
            if chunk:
                yield recommend_pb2.UploadMusicRequest(musicId=musicId, chunk=chunk)
            else:
                return

def run():
    # with grpc.insecure_channel('ip:9090') as channel:
    with grpc.insecure_channel('localhost:9090') as channel:
        stub = recommend_pb2_grpc.RecommendStub(channel)
        response = stub.UploadMusic(generate_chunks('./music/陈奕迅-粤语残片.mp3', musicId="337634739018141696"))
        logger.info("上传完成，等待服务器相应")
        logger.info(f"File upload status: {response.success}，{response.message}")

if __name__ == '__main__':
    run()
