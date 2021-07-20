package com.diditech.vrp.domain;

import com.graphhopper.jsprit.core.util.Coordinate;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Location {

    private String id;
    private Coord coord;
    private Integer index;

    /**
     * 转换对象
     */
    public Coordinate getCoordinate(){
        return Coordinate.newInstance(coord.getX(), coord.getY());
    }

}