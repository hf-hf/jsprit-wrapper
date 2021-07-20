package com.diditech.vrp.utils;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.util.Coordinate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 坐标点
 * @author hefan
 * @date 2021/7/14 14:12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Point {

    private int id;

    private double lng;

    private double lat;

    public static Point convert(Location location){
        Coordinate coordinate = location.getCoordinate();
        return new Point(Integer.valueOf(location.getId()),
                coordinate.getX(), coordinate.getY());
    }

    public Location loc() {
        return Location.Builder.newInstance()
                .setCoordinate(Coordinate.newInstance(lng, lat))
                .setIndex(id)
                .setId(id + "")
                .build();
    }

}
