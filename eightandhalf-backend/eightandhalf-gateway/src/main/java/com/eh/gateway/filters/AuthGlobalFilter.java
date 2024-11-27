package com.eh.gateway.filters;

import com.eh.gateway.GatewayApplication;
import com.eh.gateway.config.AuthProperties;
import com.eh.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter , Ordered {



    private final AuthProperties authProperties;

    private final AntPathMatcher antPathMatcher =new AntPathMatcher();


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        if(isExclude(request.getPath().toString())){
            return chain.filter(exchange);
        }

        String token = null;
        log.info("1111111");
        List<String> authorization = request.getHeaders().get("Access-Token");
        if(authorization!=null&&!authorization.isEmpty()){
            token=authorization.getFirst();
        }

        Long userId=null;

        try {
            Map<String, Object> claims = JwtUtil.parseToken(token);
            userId = (Long) claims.get("id");
            log.info("userId="+userId);
        } catch (Exception e) {
            log.error(e.getMessage());
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //传递用户信息到微服务
        String userInfo=userId.toString();
        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo)).build();


        return chain.filter(swe);
    }



    private boolean isExclude(String path) {
        for (String excludePath : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath,path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
