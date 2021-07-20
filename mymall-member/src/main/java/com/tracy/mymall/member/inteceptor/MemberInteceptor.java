package com.tracy.mymall.member.inteceptor;

import com.tracy.mymall.common.constant.AuthConst;
import com.tracy.mymall.common.vo.MemberEntityVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 用户拦截器，在查询跟当前用户相关的数据的时候处理
 */
@Component
@Slf4j
public class MemberInteceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberEntityVo> loginUser = new ThreadLocal();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/member/**", requestURI);
        if (match) {
            return true;
        }
        HttpSession session = request.getSession();
        MemberEntityVo attribute = (MemberEntityVo) session.getAttribute(AuthConst.LOGIN_USER);
        if (attribute != null) {
            loginUser.set(attribute);
            return true;
        }else {
            request.getSession().setAttribute("msg", "请先登录");
            try {
                response.sendRedirect("http://auth.mymall.com:1111/login.html");
            } catch (IOException e) {
                log.error("", e);
            }
            return false;

        }

    }
}
