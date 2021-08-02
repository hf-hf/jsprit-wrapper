package com.diditech.vrp;

import java.util.List;

import com.diditech.vrp.utils.Point;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;

public interface IPoint {

    /**
     * 获取标识
     */
    String getId();

    /**
     * 获取点集
     */
    List<Point> getPoints();

    /**
     * 获取前缀，确保ID不重复
     */
    String getPrefix();

    /**
     * 填充点ID
     * @author hefan
     * @date 2021/7/30 16:34
     */
    default void fillPointId() {
        List<Point> points = getPoints();
        if(CollectionUtil.isNotEmpty(points)){
            return;
        }
        Point point;
        for(int i=0;i < points.size();i++){
            point = points.get(i);
            if (ObjectUtil.isNotNull(point)) {
                point.setId(getPrefix() + getId() + "-" + i);
            }
        }
    }

}
