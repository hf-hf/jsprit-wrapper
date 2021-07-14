package com.diditech.vrp.vehicle;

import com.diditech.vrp.vehicle.type.BasicVehicleType;

/**
 * 四座车带时间窗口
 *
 * @author hefan
 * @date 2021/7/8 13:55
 */
public class FourSeatVehicleWithTimeWindow extends BasicVehicleWithTimeWindow {

    public FourSeatVehicleWithTimeWindow(String id) {
        super(id, new BasicVehicleType(id, 4));
    }

}
