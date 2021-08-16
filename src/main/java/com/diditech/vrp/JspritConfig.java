package com.diditech.vrp;

import com.diditech.vrp.enums.TacticsEnum;

import lombok.Getter;

/**
 * 全局配置
 * @author hefan
 * @date 2021/8/2 15:07
 */
@Getter
public class JspritConfig {

    private static JspritConfig INSTANCE;

    static {
        JspritConfig.INSTANCE = new JspritConfig();
    }

    private JspritConfig() {

    }

    public static JspritConfig getInstance() {
        return INSTANCE;
    }

    /**
     * 拼车上车的最大等待时间，默认为10分钟
     */
    int pickupMaxWaitMinutes = 10;

    /**
     * 拼车送达的最小延迟时间，默认为0分钟
     */
    int deliveryMinWaitMinutes = 0;

    /**
     * 拼车送达的最大延迟时间，默认为120分钟
     */
    int deliveryMaxWaitMinutes = 120;

    /**
     * 路线策略
     */
    TacticsEnum tactics = TacticsEnum.CONVENTIONAL_ROUTE;

    public static void setPickupMaxWaitMinutes(int pickupMaxWaitMinutes) {
        INSTANCE.pickupMaxWaitMinutes = pickupMaxWaitMinutes;
    }

    public static void setDeliveryMinWaitMinutes(int deliveryMinWaitMinutes) {
        INSTANCE.deliveryMinWaitMinutes = deliveryMinWaitMinutes;
    }

    public static void setDeliveryMaxWaitMinutes(int deliveryMaxWaitMinutes) {
        INSTANCE.deliveryMaxWaitMinutes = deliveryMaxWaitMinutes;
    }

    public static void setTactics(TacticsEnum tactics) {
        INSTANCE.tactics = tactics;
    }

}
