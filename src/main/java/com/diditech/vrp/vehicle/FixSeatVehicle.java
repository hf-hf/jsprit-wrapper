package com.diditech.vrp.vehicle;

import com.diditech.vrp.vehicle.type.BaseVehicleType;

/**
 * 六座车
 *
 * @author hefan
 * @date 2021/7/8 13:55
 */
public class FixSeatVehicle extends BaseVehicle {

    public FixSeatVehicle(String id) {
        super(id, new BaseVehicleType(id, 6));
    }

}
