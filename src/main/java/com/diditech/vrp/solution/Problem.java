package com.diditech.vrp.solution;

import java.util.List;
import java.util.Map;

import com.diditech.vrp.solution.route.Act;
import com.diditech.vrp.solution.route.Route;
import com.diditech.vrp.utils.Point;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Problem {

    private ProblemType problemType;
    private VehiclesBean vehicles;
    private VehicleTypes vehicleTypes;
    private Shipments shipments;
    private SolutionsBean solutions;
    private InitialRoutesBean initialRoutes;
    private Map<String, List<Point>> wayPointsMap;


    /**
     * 获取推荐的车辆ID
     *
     * @author hefan
     * @date 2021/7/28 15:26
     */
    public String getBestVehicleId(SolutionsBean.SolutionBean solution, String jobId) {
        String vehicleId = null;
        for (Route route : solution.getRoutes().getRoute()) {
            for (Act act : route.getAct()) {
                if (act.getShipmentId().equals(jobId)) {
                    vehicleId = route.getVehicleId();
                    break;
                }
            }
        }
        return vehicleId;
    }

    /**
     * 获取cost最少的解决方案
     *
     * @author hefan
     * @date 2021/7/28 15:25
     */
    public SolutionsBean.SolutionBean getBestSolution() {
        if (null == this.solutions) {
            return null;
        }
        SolutionsBean.SolutionBean best = null;
        for (SolutionsBean.SolutionBean s : this.solutions.getSolution()) {
            if (best == null) best = s;
            else if (s.getCost() < best.getCost()) best = s;
        }
        return best;
    }

}
