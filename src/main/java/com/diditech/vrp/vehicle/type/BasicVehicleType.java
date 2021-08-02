package com.diditech.vrp.vehicle.type;

import com.diditech.vrp.IBuilder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;

import cn.hutool.core.util.IdUtil;

/**
 * @author hefan
 * @date 2021/7/8 13:54
 */
public class BasicVehicleType implements IBuilder<VehicleType> {

    protected VehicleTypeImpl.Builder builder;

    public BasicVehicleType(int capacityDimension) {
        builder = VehicleTypeImpl.Builder.newInstance(IdUtil.simpleUUID())
                .addCapacityDimension(0, capacityDimension);
    }

    @Override
    public VehicleType build() {
        return this.builder.build();
    }

}
