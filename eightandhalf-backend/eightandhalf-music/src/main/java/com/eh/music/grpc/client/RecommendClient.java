package com.eh.music.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import proto.RecommendGrpc;
import proto.RecommendOuterClass;

import javax.annotation.PreDestroy;
import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class RecommendClient {
    private static final Logger logger = Logger.getLogger(RecommendClient.class.getName());
    private final ManagedChannel channel;
    private final RecommendGrpc.RecommendStub asyncStub;
    private final RecommendGrpc.RecommendBlockingStub blockingStub;  // 添加阻塞式存根

    // 初始化 gRPC 客户端
    public RecommendClient() {
        String host = "ip"; // 替换为实际服务端地址
        int port = 9091;          // 替换为实际服务端端口
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .enableRetry()               // 启用重试
                .maxRetryAttempts(3)
                .usePlaintext() // 开启非加密传输，适合测试
                .build();
        asyncStub = RecommendGrpc.newStub(channel);
        blockingStub = RecommendGrpc.newBlockingStub(channel);
        logger.info("连接到 gRPC 服务端 " + host + ":" + port);
    }

    // 关闭连接
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        logger.info("gRPC 连接已关闭");
    }

    @PreDestroy
    public void cleanup() throws InterruptedException {
        shutdown();
    }
    // 上传音乐文件方法

    public void uploadMusic(String musicId, MultipartFile file) throws IOException {
        StreamObserver<RecommendOuterClass.UploadMusicReply> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(RecommendOuterClass.UploadMusicReply reply) {
                logger.info("服务器响应: " + reply.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                logger.severe("上传失败: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("上传完成");
            }
        };

        StreamObserver<RecommendOuterClass.UploadMusicRequest> requestObserver = asyncStub.uploadMusic(responseObserver);
        try (var inputStream = file.getInputStream()) {
            byte[] buffer = new byte[3 * 1024 * 1024]; // 每块传输3MB
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                RecommendOuterClass.UploadMusicRequest request = RecommendOuterClass.UploadMusicRequest.newBuilder()
                        .setMusicId(musicId)
                        .setChunk(com.google.protobuf.ByteString.copyFrom(buffer, 0, bytesRead))
                        .build();
                requestObserver.onNext(request); // 发送数据块
            }
        } catch (IOException e) {
            logger.severe("读取文件时出错: " + e.getMessage());
            requestObserver.onError(e); // 如果读取文件时出错，通知服务端
            return;
        }
        // 通知服务端所有数据已发送完毕
        requestObserver.onCompleted();
    }

    public List<String> getMusic(String userId) {
        RecommendOuterClass.RecommendRequest request = RecommendOuterClass.RecommendRequest.newBuilder()
                .setUserId(userId)
                .build();
        // 调用 gRPC 服务的同步方法
        List<String> musicIdList = null;
        try {
            RecommendOuterClass.RecommendReply response = blockingStub.getMusic(request);
            // 获取并处理返回的音乐 ID 列表
            musicIdList = response.getMusicIdList(); // 获取 repeated 字段的列表
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("获取推荐音乐失败: " + e.getMessage());
        }
        return musicIdList;
    }
}

