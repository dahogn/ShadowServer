package com.runhang.shadow.client.core.exception;
/**
 * @ClassName NoTopicException
 * @Description 无主题异常
 * @Date 2019/6/19 10:19
 * @author szh
 **/
public class NoTopicException extends Exception {

    public NoTopicException() {
        super("entity's topic is null");
    }

}
