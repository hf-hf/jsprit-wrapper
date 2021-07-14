package com.diditech.vrp.vehicle;

import java.util.Date;

import com.diditech.vrp.vehicle.type.BasicVehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;

/**
 * 基础车辆带时间窗口
 *
 * @author hefan
 * @date 2021/7/14 13:47
 */
public class BasicVehicleWithTimeWindow extends BasicVehicle
        implements ITimeWindow {

    public BasicVehicleWithTimeWindow(String id, BasicVehicleType vehicleType) {
        super(id, vehicleType);
    }

    @Override
    public void setEarliestStart(Date date) {
        builder.setEarliestStart(date.getTime());
    }

    @Override
    public void setLatestArrival(Date date) {
        builder.setLatestArrival(date.getTime());
    }

    @Override
    public VehicleImpl build() {
        return this.builder.build();
    }

}
