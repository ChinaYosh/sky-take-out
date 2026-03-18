package com.sky.ascept;

import com.sky.annotation.AutoFile;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Component
@Aspect
public class AutoFileAscept
{
    @Pointcut("execution(* com.sky.mapper..*.*(..)) && @annotation(com.sky.annotation.AutoFile)")
    public  void AutoFilePointCut()
    {

    }

    /**
     * 前置通知
     */
    @Before("AutoFilePointCut()")
    public void before(JoinPoint joinPoint)
    {
        log.info("开始进行数据校验");
        //获取对象
        MethodSignature method = (MethodSignature) joinPoint.getSignature();
        AutoFile autoFile = method.getMethod().getAnnotation(AutoFile.class);
        OperationType operationType = autoFile.value();
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0)
        {
            return;
        }
        Object object = args[0];
        //准备赋值数据
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();

        if(operationType == OperationType.INSERT)
        {

            try
            {
                Method setCreateTime = object.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);

                Method setCreateUser = object.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                setCreateTime.invoke(object, now);
                setCreateUser.invoke(object, id);
            }
            catch (Exception e)
            {
                log.error(e.getMessage());
            }
        }

        if(operationType == OperationType.UPDATE || operationType == OperationType.INSERT)
        {
            try
            {
                Method setUpdateTime = object.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = object.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(object, now);
                setUpdateUser.invoke(object, id);
            }
            catch (Exception e)
            {
                log.error(e.getMessage());
            }
        }



        //根据；类型来赋值
    }
}
