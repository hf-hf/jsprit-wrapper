package com.diditech.vrp.vehicle;

import com.diditech.vrp.IBuilder;
import com.diditech.vrp.IPoints;
import com.diditech.vrp.utils.Point;
import com.diditech.vrp.vehicle.type.BasicVehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;

import lombok.Getter;

/**
 * 基础车辆
 *
 * @author hefan
 * @date 2021/7/14 13:47
 */
@Getter
public class BasicVehicle implements IBuilder<VehicleImpl>, IPoints {

    protected BasicVehicleType vehicleType;

    protected VehicleImpl.Builder builder;

    protected String id;

    protected Point startPoint;

    protected Point endPoint;

    public BasicVehicle(String id, BasicVehicleType vehicleType) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.builder = VehicleImpl.Builder.newInstance(id);
        this.builder.setType(this.vehicleType.build());
    }

    /**
     * 设置起始点
     */
    public void setStartLocation(Point point) {
        startPoint = point;
        builder.setStartLocation(point.loc());
    }

    /**
     * 设置结束点
     */
    public void setEndLocation(Point point) {
        endPoint = point;
        builder.setEndLocation(point.loc());
    }

    /**
     * 是否返回车库
     */
    public void setReturnToDepot(boolean returnToDepot) {
        builder.setReturnToDepot(returnToDepot);
    }

    @Override
    public Point[] getPoints() {
        Point[] points = new Point[1];
        points[0] = startPoint;
        return points;
    }

    @Override
    public VehicleImpl build() {
        return this.builder.build();
    }

}
