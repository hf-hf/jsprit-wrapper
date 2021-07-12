package com.diditech.vrp.vehicle;

import com.diditech.vrp.IBuilder;
import com.diditech.vrp.vehicle.type.BaseVehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;

public class BaseVehicle implements IBuilder<VehicleImpl> {

    protected BaseVehicleType vehicleType;

    protected VehicleImpl.Builder builder;

    public BaseVehicle(String id, BaseVehicleType vehicleType) {
        builder = VehicleImpl.Builder.newInstance(id);
        this.vehicleType = vehicleType;
        this.builder.setType(this.vehicleType.build());
    }

    @Override
    public VehicleImpl build() {
        return this.builder.build();
    }

}
