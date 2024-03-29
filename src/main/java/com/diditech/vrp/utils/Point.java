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

    private String id;

    private double lng;

    private double lat;

    @Deprecated
    public static Point convert(Location location){
        Coordinate coordinate = location.getCoordinate();
        return new Point(location.getId(),
                coordinate.getX(), coordinate.getY());
    }

    public Point(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public Location loc() {
        return Location.Builder.newInstance()
                .setCoordinate(Coordinate.newInstance(lng, lat))
                // 不使用，默认为0
                .setIndex(0)
                .setId(id)
                .build();
    }

    public static Point create(double lng, double lat) {
        return new Point(lng, lat);
    }

}
