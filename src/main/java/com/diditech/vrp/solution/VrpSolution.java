package com.diditech.vrp.solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.diditech.vrp.solution.route.VRoute;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.util.VehicleIndexComparator;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车辆路径问题解决方案
 *
 * @author hefan
 * @date 2021/7/14 11:03
 */
@Data
@NoArgsConstructor
public class VrpSolution {

    private double cost;

    private List<VRoute> routes;

    /**
     * 未分配订单
     * @author hefan
     * @date 2021/7/14 13:42
     */
    private List<String> unassignedJobs;

    public VrpSolution(VehicleRoutingProblemSolution solution) {
        List<VehicleRoute> list = new ArrayList<>(solution.getRoutes());
        Collections.sort(list, new VehicleIndexComparator());
        cost = solution.getCost();
        routes = list.stream()
                .map(route -> new VRoute().convert(route))
                .collect(Collectors.toList());
        unassignedJobs = solution.getUnassignedJobs().stream()
                .map(Job::getId)
                .collect(Collectors.toList());
    }

}
