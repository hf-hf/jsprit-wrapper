package com.diditech.vrp.baidu;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BaiduResponse {

    private Integer status;
    private List<ResultBean> result;
    private String message;

    @NoArgsConstructor
    @Data
    public static class ResultBean {
        private DistanceBean distance;
        private DurationBean duration;

        @NoArgsConstructor
        @Data
        public static class DistanceBean {
            private String text;
            private Integer value;
        }

        @NoArgsConstructor
        @Data
        public static class DurationBean {
            private String text;
            private Integer value;
        }
    }
}