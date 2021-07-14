package com.diditech.vrp.exception;

import com.diditech.vrp.utils.Point;

/**
 * 重复坐标点
 * @author hefan
 * @date 2021/7/14 16:22
 */
public class RepeatPointException extends RuntimeException {

    public RepeatPointException(Point point) {
        super("has same id point id:" + point.getId());
    }

}
