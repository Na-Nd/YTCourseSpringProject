package com.example.sweater.util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class RedirectInterceptor implements HandlerInterceptor {
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if(modelAndView != null){
            // Данная строка будет содержать все аргументы, которые идут в урле после знака вопроса
            String args = request.getQueryString() != null ? request.getQueryString() : "";
            String url = request.getRequestURI().toString() + "?" + args;
            response.setHeader("Turbolinks-Location", "");
        }
    }
}
