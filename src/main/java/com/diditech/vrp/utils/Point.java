package com.diditech.vrp.utils;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.util.Coordinate;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * 坐标点
 * @author hefan
 * @date 2021/7/14 14:12
 */
@Data
@AllArgsConstructor
public class Point {

    private int id;

    private double lng;

    private double lat;

    public Location loc() {
        return Location.Builder.newInstance()
                .setCoordinate(Coordinate.newInstance(lng, lat))
                .setIndex(id)
                .setId(id + "")
                .build();
    }

}
