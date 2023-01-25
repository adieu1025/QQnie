package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 * 利用的是spring的代理模式
 * AOP切面
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody //该方法返回json数据，因此引入该注解，最后会把结果封装成json数据进行返回
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法（用户名重复时的数据库异常）
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException e){
        log.error(e.getMessage());
        if(e.getMessage().contains("Duplicate entry")){
            String[] split = e.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误，操作失败！");
    }

    /**
     * 异常处理方法(删除分类时的自定义异常)
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException e){
        log.error(e.getMessage());
        return R.error(e.getMessage());
    }
}
