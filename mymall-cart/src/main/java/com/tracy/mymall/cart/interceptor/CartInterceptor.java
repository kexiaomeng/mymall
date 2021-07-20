package com.tracy.mymall.cart.interceptor;

import com.tracy.mymall.cart.vo.UserInfoTo;
import com.tracy.mymall.common.constant.AuthConst;
import com.tracy.mymall.common.vo.MemberEntityVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 拦截器拦截请求需要在springmvc的配置中配置
 *
 * 拦截器里判断用户是否登录，如果没登录，则判断cookie是否带了临时用户，如果没有，则创建一个临时用户放入cookie
 */
public class CartInterceptor implements HandlerInterceptor {
    /**
     * 用来在同一线程里共享数据，controler能获取到此处封装的数据
     */
    public static final ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
    /**
     * 拦截请求，判断用户是否登录，如果没有，则添加临时用户，如果登录了，则取出用户信息
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MemberEntityVo memberEntityVo = (MemberEntityVo) request.getSession().getAttribute(AuthConst.LOGIN_USER);
        UserInfoTo userInfoTo = new UserInfoTo();
        if (memberEntityVo != null) {
            userInfoTo.setUserId(memberEntityVo.getId());
        }
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AuthConst.TEMP_USER_KEY.equals(cookie.getName())) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(false);
                }
            }
        }

        // 创建一个临时用户
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            userInfoTo.setUserKey(UUID.randomUUID().toString());
            userInfoTo.setTempUser(true);

        }

        threadLocal.set(userInfoTo);

        return true;
    }

    /**
     * 执行完毕之后分配临时用户让浏览器保存
     */
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

        UserInfoTo userInfoTo = threadLocal.get();
        // 如果是临时用户，返回临时购物车的cookie
        if(userInfoTo.getTempUser()){
            Cookie cookie = new Cookie(AuthConst.TEMP_USER_KEY, userInfoTo.getUserKey());
            // 设置这个cookie作用域 过期时间
            cookie.setDomain("mymall.com");
            cookie.setMaxAge(30 * 24 * 60 * 60);
            response.addCookie(cookie);
        }
        // threadLocal用完后删除，防止可能会产生value的内存泄露
        threadLocal.remove();
    }

}
