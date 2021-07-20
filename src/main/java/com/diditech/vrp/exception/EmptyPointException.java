package com.diditech.vrp.exception;

/**
 * 空值点
 * @author hefan
 * @date 2021/7/14 16:22
 */
public class EmptyPointException extends RuntimeException {

    public EmptyPointException() {
        super("point is null");
    }

}
