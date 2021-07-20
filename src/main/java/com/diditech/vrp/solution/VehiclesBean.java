package com.diditech.vrp.solution;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class VehiclesBean {
    private List<Vehicle> vehicle;

    @NoArgsConstructor
    @Data
    public static class Vehicle {
        private String id;
        private String typeId;
        private StartLocation startLocation;
        private EndLocation endLocation;
        private TimeSchedule timeSchedule;
        private String returnToDepot;

        @NoArgsConstructor
        @Data
        public static class StartLocation {
            private String id;
            private Coord coord;
            private String index;

        }

        @NoArgsConstructor
        @Data
        public static class EndLocation {
            private String id;
            private Coord coord;
            private Integer index;

        }

        @NoArgsConstructor
        @Data
        public static class TimeSchedule {
            private Double start;
            private Double end;
        }
    }
}