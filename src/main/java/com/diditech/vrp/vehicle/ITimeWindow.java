package com.diditech.vrp.vehicle;

import java.util.Date;

/**
 * 时间窗口
 *
 * @author hefan
 * @date 2021/7/14 14:34
 */
public interface ITimeWindow {

    /**
     * 设置最早开始时间
     */
    void setEarliestStart(Date date);

    /**
     * 设置最晚到达时间
     */
    void setLatestArrival(Date date);

}
