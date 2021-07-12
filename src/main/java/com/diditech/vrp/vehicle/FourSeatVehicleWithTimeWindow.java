package com.diditech.vrp.vehicle;

import java.util.Date;

import com.diditech.vrp.geo.Point;
import com.diditech.vrp.vehicle.type.BaseVehicleType;

/**
 * 四座车
 *
 * @author hefan
 * @date 2021/7/8 13:55
 */
public class FourSeatVehicleWithTimeWindow extends BaseVehicle
        implements ITimeWindow<FourSeatVehicleWithTimeWindow> {

    public FourSeatVehicleWithTimeWindow(String id) {
        super(id, new BaseVehicleType(id, 4));
    }

    @Override
    public FourSeatVehicleWithTimeWindow setEarliestStart(Date date) {
        builder.setEarliestStart(date.getTime());
        System.out.println(date.getTime());
        return this;
    }

    @Override
    public FourSeatVehicleWithTimeWindow setLatestArrival(Date date) {
        builder.setLatestArrival(date.getTime());
        System.out.println(date.getTime());
        return this;
    }

    public FourSeatVehicleWithTimeWindow setReturnToDepot(boolean returnToDepot) {
        builder.setReturnToDepot(returnToDepot);
        return this;
    }

    public FourSeatVehicleWithTimeWindow setStartLocation(Point point) {
        builder.setStartLocation(point.loc());
        return this;
    }

}
