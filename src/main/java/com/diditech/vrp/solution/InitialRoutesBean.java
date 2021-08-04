package com.diditech.vrp.solution;

import java.util.List;

import com.diditech.vrp.solution.route.Act;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class InitialRoutesBean {
    private List<RouteBean> route;


    @NoArgsConstructor
    @Data
    public static class RouteBean {
        private String driverId;
        private String vehicleId;
        private Double start;
        private List<Act> act;
        private Double end;
    }
}