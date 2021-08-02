package com.diditech.vrp;

import com.diditech.vrp.enums.TacticsEnum;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import lombok.Data;

/**
 * 全局配置
 * @author hefan
 * @date 2021/8/2 15:07
 */
@Data
public class JspritConfig {

    private static JspritConfig INSTANCE;

    private static Setting setting;

    static {
        setting = new Setting("jsprit.setting");
        JspritConfig.INSTANCE = new JspritConfig();
        String baiduAk = setting.getStr("baidu.ak");
        if(StrUtil.isNotBlank(baiduAk)){
            INSTANCE.setBaiduAk(baiduAk);
        }
        Integer tacticsInt = setting.getInt("tactics");
        if(ObjectUtil.isNotNull(tacticsInt)){
            TacticsEnum tactics = TacticsEnum.get(tacticsInt);
            if(ObjectUtil.isNotNull(tactics)){
                INSTANCE.setTactics(tactics);
            }
        }
        Integer pickup_wait_minutes = setting.getInt("pickup_wait_minutes");
        if(ObjectUtil.isNotNull(pickup_wait_minutes)){
            INSTANCE.setPickup_wait_minutes(pickup_wait_minutes);
        }
        Integer delivery_wait_start_minutes = setting.getInt("delivery_wait_start_minutes");
        if(ObjectUtil.isNotNull(delivery_wait_start_minutes)){
            INSTANCE.setDelivery_wait_start_minutes(delivery_wait_start_minutes);
        }
        Integer delivery_wait_end_minutes = setting.getInt("delivery_wait_end_minutes");
        if(ObjectUtil.isNotNull(delivery_wait_end_minutes)){
            INSTANCE.setDelivery_wait_end_minutes(delivery_wait_end_minutes);
        }
    }

    private JspritConfig() {

    }

    public static JspritConfig getInstance() {
        return INSTANCE;
    }

    String baiduAk = "gUMFqL9vB5Vf3etWx9G8nBju1yu281nL";//"3lBS83HG7YYqCf31stUsYHISNCBb2c2a";//"AsuZbkj6YlYI7tDGkomXVeMUb9ypdPdm";

    /**
     * 路线策略
     */
    TacticsEnum tactics = TacticsEnum.CONVENTIONAL_ROUTE;

    /**
     * 10分钟内能够上车
     */
    int pickup_wait_minutes = 10;

    /**
     * 送达目的前的等候时间，起始
     * 默认20~120分钟内能够送达目的地
     */
    int delivery_wait_start_minutes = 20;

    /**
     * 送达目的前的等候时间，结束
     * 默认20~120分钟内能够送达目的地
     */
    int delivery_wait_end_minutes = 120;

}
