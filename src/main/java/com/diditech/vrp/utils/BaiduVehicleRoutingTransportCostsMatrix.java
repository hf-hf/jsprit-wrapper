package com.diditech.vrp.utils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.diditech.vrp.IBuilder;
import com.diditech.vrp.baidu.BaiduApi;
import com.diditech.vrp.baidu.BaiduResponse;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;

/**
 * 百度车辆路径运输成本矩阵
 *
 * @author hefan
 * @date 2021/7/8 14:55
 */
public class BaiduVehicleRoutingTransportCostsMatrix extends AbstractForwardVehicleRoutingTransportCosts
        implements IBuilder<FastVehicleRoutingTransportCostsMatrix> {

    private FastVehicleRoutingTransportCostsMatrix.Builder builder;

    private FastVehicleRoutingTransportCostsMatrix matrix;

    private List<Location> list;

    public BaiduVehicleRoutingTransportCostsMatrix(List<Location> list, boolean isSymmetric) {
        this.list = list.stream()
                .sorted(Comparator.comparing(Location::getId))
                .collect(Collectors.toList());
        this.builder = FastVehicleRoutingTransportCostsMatrix.Builder
                .newInstance(this.list.size(), isSymmetric);
        load();
        // 判断是否加载成功
        this.matrix = this.builder.build();
    }

    private void load() {
        BaiduResponse response = BaiduApi.routeMatrix(list);
        if (0 != response.getStatus()) {
            return;
        }
        List<BaiduResponse.ResultBean> result = response.getResult();
        int index = 0;
        double distance;
        double duration;
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                distance = result.get(index).getDistance().getValue();
                duration = result.get(index).getDuration().getValue() * 1000;
                builder.addTransportTimeAndDistance(i, j, duration, distance);
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