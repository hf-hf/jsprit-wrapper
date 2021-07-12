package com.diditech.vrp.geo;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.util.Coordinate;

import lombok.AllArgsConstructor;

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
