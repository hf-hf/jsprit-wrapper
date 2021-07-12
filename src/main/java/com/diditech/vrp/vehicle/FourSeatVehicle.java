package com.diditech.vrp.vehicle;

import com.diditech.vrp.vehicle.type.BaseVehicleType;

/**
 * 四座车
 *
 * @author hefan
 * @date 2021/7/8 13:55
 */
public class FourSeatVehicle extends BaseVehicle {

    public FourSeatVehicle(String id) {
        super(id, new BaseVehicleType(id, 4));
    }

}
