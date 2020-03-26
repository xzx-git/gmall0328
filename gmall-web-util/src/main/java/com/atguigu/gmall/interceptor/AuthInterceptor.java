package com.atguigu.gmall.interceptor;

import com.atguigu.gmall.annotation.LoginRequire;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpClientUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler) throws Exception {
        //判断当前访问的方法是否需要认证拦截
        HandlerMethod method = (HandlerMethod)handler;
        LoginRequire methodAnnotation = method.getMethodAnnotation(LoginRequire.class);
        if (methodAnnotation == null){
            return true;
        }

        String token = "";
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        String newToken = request.getParameter("newToken");

//        oldToken不空，新token空， 用户登陆过
//        oldToken空，新token不空，用户第一次登陆
//        oldToken空，新token空，用户从没登陆
//        oldToken不空，新token不空，用户登录过期
        if (StringUtils.isNotBlank(oldToken)&&StringUtils.isBlank(newToken)){
            //登录过
            token=oldToken;
        }
        if (StringUtils.isBlank(oldToken)&&StringUtils.isNotBlank(newToken)){
            //第一次登录
            token=newToken;
        }

        if (StringUtils.isNotBlank(oldToken)&&StringUtils.isNotBlank(newToken)){
            //登录过期
            token=newToken;
        }


        if (methodAnnotation.ifNeedSuccess()&&StringUtils.isBlank(token)){
            StringBuffer requestURL = request.getRequestURL();//请求地址
            response.sendRedirect("http://localhost:8085/index?returnURL="+requestURL);
            return false;
        }

        String success = "";
        if (StringUtils.isNotBlank(token)){
            //远程访问passport，验证token
            success = HttpClientUtil.doGet("http://localhost:8085/verify?token="+token+"&salt="+getMyIp(request));
        }

        if (!success.equals("success")&&methodAnnotation.ifNeedSuccess()){
            response.sendRedirect("http://localhost:8085/index");
            return false;
        }

        if (!success.equals("success")&&!methodAnnotation.ifNeedSuccess())
        {
            //购物车方法
            return true;
        }

        if (success.equals("success")){
            //cookie验证通过，重新刷新cookie的过期时间
            CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);

            Map userMap = JwtUtil.decode("atguigu0328", token, getMyIp(request));
            request.setAttribute("userId",userMap.get("userId"));
            request.setAttribute("nickName",userMap.get("nickName"));
        }
        return true;
    }

    private String getMyIp(HttpServletRequest request) {
        String ip= "";
        ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();
        }
        if (StringUtils.isBlank(ip)){
            ip = "127.0.0.1";
        }
        return ip;
    }
}
