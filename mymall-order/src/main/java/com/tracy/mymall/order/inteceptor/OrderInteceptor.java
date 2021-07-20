package com.tracy.mymall.order.inteceptor;

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
 * 订单拦截器，保证在接入订单模块前用户处于登录状态
 */
@Component
@Slf4j
public class OrderInteceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberEntityVo> loginUser = new ThreadLocal();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/query/**", requestURI);
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
