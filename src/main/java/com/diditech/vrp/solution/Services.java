package com.diditech.vrp.solution;

import java.util.List;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Services {
    private List<ServiceBean> service;

    @NoArgsConstructor
    @Data
    public static class ServiceBean {
        private String id;
        private String type;
        private LocationBean location;
        @Alias("capacity-dimensions")
        private Services.ServiceBean.CapacitydimensionsBean capacitydimensions;
        private Double duration;
        private TimeWindowsBean timeWindows;

        @NoArgsConstructor
        @Data
        public static class LocationBean {
            private String id;
            private CoordBean coord;
            private String index;

            @NoArgsConstructor
            @Data
            public static class CoordBean {
                private Double x;
                private Double y;
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
}