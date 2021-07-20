package com.diditech.vrp.solution;

import java.util.List;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class VehicleTypes {
    private List<TypeBean> type;

    @NoArgsConstructor
    @Data
    public static class TypeBean {
        private String id;
        @Alias("capacity-dimensions")
        private CapacitydimensionsBean capacitydimensions;
        private CostsBean costs;

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
        public static class CostsBean {
            private Double fixed;
            private Double distance;
            private Double time;
            private Double service;
            private Double wait;
        }
    }
}