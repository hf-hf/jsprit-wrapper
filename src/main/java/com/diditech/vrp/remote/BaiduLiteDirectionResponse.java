package com.diditech.vrp.remote;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BaiduLiteDirectionResponse {

    private Integer status;
    private String message;
    private ResultBean result;

    @NoArgsConstructor
    @Data
    public static class ResultBean {
        private OriginBean origin;
        private DestinationBean destination;
        private List<RoutesBean> routes;

        @NoArgsConstructor
        @Data
        public static class OriginBean {
            private Double lng;
            private Double lat;
        }

        @NoArgsConstructor
        @Data
        public static class DestinationBean {
            private Double lng;
            private Double lat;
        }

        @NoArgsConstructor
        @Data
        public static class RoutesBean {
            private Integer distance;
            private Integer duration;
            private Integer traffic_condition;
            private Integer toll;
            private RestrictionInfoBean restriction_info;
            private List<StepsBean> steps;

            @NoArgsConstructor
            @Data
            public static class RestrictionInfoBean {
                private Integer status;
            }

            @NoArgsConstructor
            @Data
            public static class StepsBean {
                private Integer leg_index;
                private Integer distance;
                private Integer duration;
                private Integer direction;
                private Integer turn;
                private Integer road_type;
                private String road_types;
                private String instruction;
                private String path;
                private List<TrafficConditionBean> traffic_condition;
                private StartLocationBean start_location;
                private EndLocationBean end_location;

                @NoArgsConstructor
                @Data
                public static class StartLocationBean {
                    private String lng;
                    private String lat;
                }

                @NoArgsConstructor
                @Data
                public static class EndLocationBean {
                    private String lng;
                    private String lat;
                }

                @NoArgsConstructor
                @Data
                public static class TrafficConditionBean {
                    private Integer status;
                    private Integer geo_cnt;
                }
            }
        }
    }

}
