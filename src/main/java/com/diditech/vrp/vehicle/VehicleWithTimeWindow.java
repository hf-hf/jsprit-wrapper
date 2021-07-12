package com.diditech.vrp.vehicle;

import java.util.Date;

import com.diditech.vrp.geo.Point;
import com.diditech.vrp.vehicle.type.BaseVehicleType;

public class VehicleWithTimeWindow extends BaseVehicle
        implements ITimeWindow<VehicleWithTimeWindow> {

    public VehicleWithTimeWindow(String id, BaseVehicleType vehicleType) {
        super(id, vehicleType);
    }

    @Override
    public VehicleWithTimeWindow setEarliestStart(Date date) {
        builder.setEarliestStart(date.getTime());
        return this;
    }

    @Override
    public VehicleWithTimeWindow setLatestArrival(Date date) {
        builder.setEarliestStart(date.getTime());
        return this;
    }

    public VehicleWithTimeWindow setReturnToDepot(boolean returnToDepot) {
        builder.setReturnToDepot(returnToDepot);
        return this;
    }

    public VehicleWithTimeWindow setStartLocation(Point point) {
        builder.setStartLocation(point.loc());
        return this;
    }

}
