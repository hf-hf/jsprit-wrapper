package com.diditech.vrp.solution;

import java.util.List;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Shipments {
    private List<Shipment> shipment;

    @NoArgsConstructor
    @Data
    public static class Shipment {
        private String id;
        private String name;
        private PickupBean pickup;
        private DeliveryBean delivery;
        @Alias("capacity-dimensions")
        private CapacitydimensionsBean capacitydimensions;

        @NoArgsConstructor
        @Data
        public static class PickupBean {
            private Location location;
            private Double duration;
            private TimeWindowsBean timeWindows;

            @NoArgsConstructor
            @Data
            public static class TimeWindowsBean {
                private TimeWindowBean timeWindow;

                @NoArgsConstructor
                @Data
                public static class TimeWindowBean {
                    private Double start;
                    private Double end;
                }
            }
        }

        @NoArgsConstructor
        @Data
        public static class DeliveryBean {
            private Location location;
            private Double duration;
            private TimeWindowsBean timeWindows;

            @NoArgsConstructor
            @Data
            public static class TimeWindowsBean {
                private TimeWindowBean timeWindow;

                @NoArgsConstructor
                @Data
                public static class TimeWindowBean {
                    private double start;
                    private double end;
                }
            }
        }

        @NoArgsConstructor
        @Data
        public static class CapacitydimensionsBean {
            private DimensionBean dimension;

            @NoArgsConstructor
            @Data
            public static class DimensionBean {
                private Integer index;

                private Integer content;
            }
        }
    }
}
