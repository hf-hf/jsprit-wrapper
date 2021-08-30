package com.diditech.vrp.solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.diditech.vrp.solution.route.Act;
import com.diditech.vrp.solution.route.Route;
import com.diditech.vrp.utils.Constants;
import com.diditech.vrp.utils.Point;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Problem {

    private ProblemType problemType;
    private VehiclesBean vehicles;
    private VehicleTypes vehicleTypes;
    private Shipments shipments;
    private Services services;
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

    /**
     * 获取可用车辆
     *
     * @author hefan
     * @date 2021/8/30 10:29
     */
    public List<String> listAvailableVehicles(String jobId) {
        List<String> idList = new ArrayList<>();
        if (StrUtil.isBlank(jobId)) {
            return idList;
        }
        SolutionsBean solutionsBean = this.getSolutions();
        for (SolutionsBean.SolutionBean solution : solutionsBean.getSolution()) {
            SolutionsBean.SolutionBean.RoutesBean routesBean = solution.getRoutes();
            for (Route route : routesBean.getRoute()) {
                for (Act act : route.getAct()) {
                    if (act.getShipmentId().equals(jobId)) {
                        idList.add(route.getVehicleId());
                        break;
                    }
                }
            }
        }
        return idList;
    }

    /**
     * 获取主作业单ID
     *
     * @author hefan
     * @date 2021/8/30 10:57
     */
    public String getMainJobId(String vehicleId) {
        String name = Constants.MAIN_JOB_NAME_PREFIX + vehicleId;
        for (Shipments.Shipment shipment : this.getShipments().getShipment()) {
            if (name.equals(shipment.getName())) {
                return shipment.getId();
            }
        }
        return null;
    }

}
