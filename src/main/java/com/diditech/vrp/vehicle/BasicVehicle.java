package com.diditech.vrp.vehicle;

import com.diditech.vrp.IBuilder;
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
public class BasicVehicle implements IBuilder<VehicleImpl> {

    protected BasicVehicleType vehicleType;

    protected VehicleImpl.Builder builder;

    protected String id;

    protected Point startPoint;

    protected Point endPoint;

    public BasicVehicle(String id, BasicVehicleType vehicleType) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.builder = VehicleImpl.Builder.newInstance(id)
                .setType(this.vehicleType.build());
    }

    /**
     * 设置起始点
     */
    public void setStartLocation(Point point) {
        startPoint = point;
    }

    /**
     * 设置结束点
     */
    public void setEndLocation(Point point) {
        endPoint = point;
    }

    /**
     * 是否返回车库
     */
    public void setReturnToDepot(boolean returnToDepot) {
        builder.setReturnToDepot(returnToDepot);
    }

    @Override
    public VehicleImpl build() {
        this.builder.setStartLocation(startPoint.loc());
        if(null != endPoint) {
            this.builder.setStartLocation(endPoint.loc());
        }
        return this.builder.build();
    }

}
