package com.itheima.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经登陆
 */
@Slf4j
@WebFilter(filterName = "LonginCheckFilter",urlPatterns = "/*")
public class LonginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截到的请求：{}",requestURI);
        //定义不需要被拦截的请求路径uri
        String [] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };

        //2、判断本次请求是否需要处理
        boolean check = check(urls,requestURI);

        //3、如果不需要处理，直接放行
        if(check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,servletResponse);
            return;
        }

        //4-1、判断后台管理员登陆状态，如果已经登陆，则直接放行
       if(request.getSession().getAttribute("employee") != null){
           Long empId = (Long) request.getSession().getAttribute("employee");
           log.info("用户已登陆，用户id为：{}",empId);

           //设置当前线程的线程局部变量的值（用户id）
           BaseContext.setCurrentId(empId);

           filterChain.doFilter(request,response);
           return;
       }

        //4-2、判断移动端用户登陆状态，如果已经登陆，则直接放行
        if(request.getSession().getAttribute("user") != null){
            Long userId = (Long) request.getSession().getAttribute("user");
            log.info("用户已登陆，用户id为：{}",userId);

            //设置当前线程的线程局部变量的值（用户id）
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

       log.info("用户未登录");
        //5、如果未登陆则返回未登陆的结果,通过输出流的方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls){
            boolean match = PATH_MATCHER.match(url,requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
