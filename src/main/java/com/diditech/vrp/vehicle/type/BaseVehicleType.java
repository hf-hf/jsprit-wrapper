package com.diditech.vrp.vehicle.type;

import com.diditech.vrp.IBuilder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;

/**
 *
 * @author hefan
 * @date 2021/7/8 13:54
 */
public class BaseVehicleType implements IBuilder<VehicleType> {

    protected VehicleTypeImpl.Builder builder;

    public BaseVehicleType(String id, int seatNumber) {
        builder = VehicleTypeImpl.Builder.newInstance(id)
                .addCapacityDimension(0 , seatNumber);
    }

    @Override
    public VehicleType build() {
        return this.builder.build();
    }
}
