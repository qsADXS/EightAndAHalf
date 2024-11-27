package com.eh.chat.server.handler;

import com.eh.chat.config.NettyConfig;
import com.eh.chat.mapper.ChatMapper;
import com.eh.chat.model.entity.ChatMessage;
import com.eh.common.util.JwtUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ChannelHandler.Sharable
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Value("${webSocket.netty.path:/webSocket}")
    private String wsUri;

    /**
     * 一旦连接，第一个被执行
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的客户端链接：[{}]", ctx.channel().id().asLongText());
        // 添加到channelGroup 通道组
        NettyConfig.getChannelGroup().add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        URI uri = new URI(request.uri());

        // 检查路径是否匹配
        if (wsUri.equalsIgnoreCase(uri.getPath())) {

            // 提取 Query 参数中的 accessToken
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
            Map<String, List<String>> params = queryStringDecoder.parameters();
            List<String> accessTokenList = params.get("accessToken");
            String token = new String();
            if (accessTokenList != null && !accessTokenList.isEmpty()) {
                token = accessTokenList.get(0);
            }
            // 解析 token
            Map<String, Object> claims = null;
            try {

                claims = JwtUtil.parseToken(token);
            } catch (Exception e) {
                log.info("token过期");
                // token过期，返回 401
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                return; // Token 无效时直接返回，不继续执行后续代码
            }

            Long id = (Long) claims.get("id");

            log.info("用户 ID: " + id);

            // 将用户ID加入到 channel 的属性中
            AttributeKey<String> key = AttributeKey.valueOf("id");
            ctx.channel().attr(key).setIfAbsent(id.toString());

            // 将 channel 放入共享集合
            NettyConfig.getChannelMap().put(id.toString(), ctx.channel());
            //NettyConfig.getChannelGroup().writeAndFlush(new TextWebSocketFrame("用户 " + id + " 认证成功"));


            log.info("chua");
            // 继续传递请求给 WebSocket 握手处理器
            // 重构请求 URL，去除参数部分

            // 构建新的请求，不带 Query 参数
            URI newUri = new URI(wsUri);
            FullHttpRequest newRequest = new DefaultFullHttpRequest(
                    request.protocolVersion(), request.method(), newUri.toASCIIString(), request.content().retain());

            // 复制原始请求的头信息到新请求
            newRequest.headers().set(request.headers());

            // 将请求传递到 WebSocket 握手处理器
            ctx.fireChannelRead(newRequest);

        } else {
            // 请求路径不匹配，返回 404
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }


}


