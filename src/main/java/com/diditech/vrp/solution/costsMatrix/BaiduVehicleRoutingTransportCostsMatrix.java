package com.diditech.vrp.solution.costsMatrix;

import java.util.List;
import java.util.Map;

import com.diditech.vrp.IBuilder;
import com.diditech.vrp.enums.TacticsEnum;
import com.diditech.vrp.remote.BaiduApi;
import com.diditech.vrp.remote.BaiduLiteDirectionResponse;
import com.diditech.vrp.utils.Point;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

/**
 * 百度车辆路径运输成本矩阵
 *
 * @author hefan
 * @date 2021/7/8 14:55
 */
public class BaiduVehicleRoutingTransportCostsMatrix
        extends AbstractForwardVehicleRoutingTransportCosts
        implements IBuilder<VehicleRoutingTransportCostsMatrix> {

    private VehicleRoutingTransportCostsMatrix.Builder builder;

    private VehicleRoutingTransportCostsMatrix matrix;

    private Map<String, Coordinate> map;

    private Map<String, List<Point>> wayPointsMap;

    private TacticsEnum tactics;

    public BaiduVehicleRoutingTransportCostsMatrix(Map<String, Coordinate> locationMap,
                                                   boolean isSymmetric,
                                                   TacticsEnum tacticsEnum,
                                                   Map<String, List<Point>> wayPointsMap) {
        this.map = locationMap;
        this.tactics = tacticsEnum;
        this.wayPointsMap = wayPointsMap;
        this.builder = VehicleRoutingTransportCostsMatrix.Builder
                .newInstance(isSymmetric);
        this.load();
        // 判断是否加载成功
        this.matrix = this.builder.build();
    }

    private void load() {
        double distance;
        double duration;
        Coordinate fromCoord;
        Coordinate toCoord;
        for (String from : map.keySet()) {
            fromCoord = map.get(from);
            for (String to : map.keySet()) {
                toCoord = map.get(to);
                BaiduLiteDirectionResponse response =
                        BaiduApi.liteDirectionDriving(fromCoord, toCoord,
                                tactics, wayPointsMap.get(to));
                if (0 != response.getStatus()) {
                    continue;
                }
                List<BaiduLiteDirectionResponse.ResultBean.RoutesBean> result = response.getResult().getRoutes();
                distance = result.get(0).getDistance();
                duration = result.get(0).getDuration() * 1000;
                builder.addTransportDistance(from, to, distance);
                builder.addTransportTime(from, to, duration);
                //System.out.println(from + "-" + to);
            }
        }
    }

    @Override
    public VehicleRoutingTransportCostsMatrix build() {
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