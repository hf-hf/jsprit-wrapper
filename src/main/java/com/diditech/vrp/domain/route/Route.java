package com.diditech.vrp.domain.route;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Route {

    private String driverId;

    private String vehicleId;

    private Long start;

    private List<Act> act;

    private Long end;

}