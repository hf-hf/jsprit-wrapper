package com.diditech.vrp.solution.costsMatrix;

import java.util.List;
import java.util.Map;

import com.diditech.vrp.IBuilder;
import com.diditech.vrp.remote.BaiduApi;
import com.diditech.vrp.remote.BaiduResponse;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;

/**
 * 百度车辆路径运输成本矩阵
 *
 * @author hefan
 * @date 2021/7/8 14:55
 */
public class BatchBaiduVehicleRoutingTransportCostsMatrix extends AbstractForwardVehicleRoutingTransportCosts
        implements IBuilder<FastVehicleRoutingTransportCostsMatrix> {

    private FastVehicleRoutingTransportCostsMatrix.Builder builder;

    private FastVehicleRoutingTransportCostsMatrix matrix;

    private Map<String, Coordinate> map;

    public BatchBaiduVehicleRoutingTransportCostsMatrix(Map<String, Coordinate> locationMap, boolean isSymmetric) {
        this.map = locationMap;
        this.builder = FastVehicleRoutingTransportCostsMatrix.Builder
                .newInstance(locationMap.size(), isSymmetric);
        load();
        // 判断是否加载成功
        this.matrix = this.builder.build();
    }

    private void load() {
        BaiduResponse response = BaiduApi.routeMatrix(map);
        if (0 != response.getStatus()) {
            return;
        }
        List<BaiduResponse.ResultBean> result = response.getResult();
        int index = 0;
        double distance;
        double duration;
        int fromN;
        int toN;
        for (String from : map.keySet()) {
            fromN = Integer.parseInt(from);
            for (String to : map.keySet()) {
                toN = Integer.parseInt(to);
                distance = result.get(index).getDistance().getValue();
                duration = result.get(index).getDuration().getValue() * 1000;
                builder.addTransportTimeAndDistance(fromN, toN, duration, distance);
                index++;
            }
        }
    }

    @Override
    public FastVehicleRoutingTransportCostsMatrix build() {
        return this.matrix;
    }

    @Override
    public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
        return this.matrix.getDistance(from, to, departureTime, vehicle);
    }

    @Override
    public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
        return this.matrix.getTransportTime(from, to, departureTime, driver, vehicle);
    }

    @Override
    public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
        return this.matrix.getTransportTime(from, to, departureTime, driver, vehicle);
    }

}