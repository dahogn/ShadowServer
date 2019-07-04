package com.runhang.shadow.client.core.exception;
/**
 * @ClassName NoSriException
 * @Description 无sri异常
 * @Date 2019/7/2 23:38
 * @author szh
 **/
public class NoSriException extends Exception {

    public NoSriException() {
        super("entity's shadow resource identifier is null");
    }

}
