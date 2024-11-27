package com.eh.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.eh.common.util.ThreadLocalUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String userInfo = request.getHeader("user-info");

        if(StrUtil.isNotBlank(userInfo)){
            ThreadLocalUtil.set(Long.valueOf(userInfo));
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            ThreadLocalUtil.remove();
    }
}
