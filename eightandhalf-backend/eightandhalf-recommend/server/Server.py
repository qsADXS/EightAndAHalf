#! /usr/bin/env python
# -*- coding: utf-8 -*-
import logging
import os
import time
import random
import grpc
from concurrent import futures

from proto import recommend_pb2_grpc
from proto import recommend_pb2
from mysql_utils import db_operations
from config.logger_config import setup_logger
from data.MusicFeature import extractFeatures
from recommend.Music import getRecommendMusic

_ONE_DAY_IN_SECONDS = 60 * 60 * 24
logger = setup_logger("server:9090", '../log')


class Recommend(recommend_pb2_grpc.RecommendServicer):
    def GetMusic(self, request, context):
        logger.info("GetMusic")
        logger.info(f"客户端ip：{context.peer()}")
        if not request.userId:
            logger.error("UserId is null")
            context.abort(grpc.StatusCode.NOT_FOUND, "UserId is null")
        logger.info("用户id：" + request.userId)
        try:
            # musics = db_operations.find_music(request.userId)
            musics = getRecommendMusic(request.userId)

            logger.info("获取到数据")
            return recommend_pb2.RecommendReply(musicId=[str(num) for num in musics])
        except Exception as e:
            logger.error(e)
            context.abort(grpc.StatusCode.UNKNOWN, e)

    def UploadMusic(self, request_iterator, context):
        logger.info("UploadMusic")
        logger.info(f"客户端ip：{context.peer()}")
        # 从第一个请求中获取 musicId 和文件名
        first_request = next(request_iterator)
        musicId = first_request.musicId
        filename = f"./data/music/mp3/{musicId}.mp3"
        logger.info(f"文件名{filename}")
        i = 0
        with open(filename, 'wb') as f:
            # 写入第一个请求的文件块
            f.write(first_request.chunk)
            logger.info("写入文件")
            i += 1
            # 处理后续的文件块
            for request in request_iterator:
                f.write(request.chunk)
                i += 1
            f.close()
        if os.path.exists(filename):
            logger.info(f"文件接收完成,接受次数{i}")
        else:
            delete(filename)
            return recommend_pb2.UploadMusicReply(success=False,message='文件保存失败')
        try:
            if not extractFeatures(musicId):
                delete(filename)
                return recommend_pb2.UploadMusicReply(success=False,message='文件提取特征失败')
        except Exception as e:
            logger.error(e)
            delete(filename)
            context.abort(grpc.StatusCode.UNKNOWN, e)
        delete(filename)
        return recommend_pb2.UploadMusicReply(success=True)


def serve():
    logger.info("开始监听9090端口")
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    recommend_pb2_grpc.add_RecommendServicer_to_server(Recommend(), server)
    server.add_insecure_port('[::]:9090')
    server.start()
    try:
        while True:
            time.sleep(_ONE_DAY_IN_SECONDS)
    except KeyboardInterrupt:
        server.stop(0)


def randomList(count=10, min_value=1, max_value=100, float_numbers=False):
    if float_numbers:
        # 生成浮点数列表，并将每个浮点数转换为字符串
        return [str(random.uniform(min_value, max_value)) for _ in range(count)]
    else:
        # 生成整数列表，并将每个整数转换为字符串
        return [str(random.randint(min_value, max_value)) for _ in range(count)]

def delete(filename):
    if os.path.exists(filename):
        os.remove(filename)
        logger.warning(f"Deleted existing file: {filename}")

if __name__ == '__main__':
    serve()
