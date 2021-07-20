package com.diditech.vrp.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IdUtils {

    private static final Snowflake snowflake = IdUtil.createSnowflake(1, 1);

    public long nextId(){
        return snowflake.nextId();
    }

}
