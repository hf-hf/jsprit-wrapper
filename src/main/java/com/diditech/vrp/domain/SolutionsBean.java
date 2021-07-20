package com.diditech.vrp.domain;

import java.util.List;

import com.diditech.vrp.domain.route.Route;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SolutionsBean {
    private List<SolutionBean> solution;

    @NoArgsConstructor
    @Data
    public static class SolutionBean {
        private Double cost;
        private RoutesBean routes;
        private UnassignedJobsBean unassignedJobs;

        @NoArgsConstructor
        @Data
        public static class RoutesBean {
            private List<Route> route;
        }

        @NoArgsConstructor
        @Data
        public static class UnassignedJobsBean {
            private List<JobBean> job;

            @NoArgsConstructor
            @Data
            public static class JobBean {
                private String id;
            }
        }

    }
}