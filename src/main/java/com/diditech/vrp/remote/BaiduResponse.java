package com.diditech.vrp.remote;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 百度请求响应
 * @author hefan
 * @date 2021/7/14 15:40
 */
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
