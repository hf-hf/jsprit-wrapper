package com.diditech.vrp.job;

import java.util.Date;

import com.diditech.vrp.IBuilder;
import com.diditech.vrp.geo.Point;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

/**
 * 装货作业
 *
 * @author hefan
 * @date 2021/7/8 14:37
 */
public class ShipmentJob implements IBuilder {

    protected Shipment.Builder builder;

    public ShipmentJob(String id, int sizeDimension, Point pickupPoint, Point deliveryPoint) {
        this.builder = Shipment.Builder.newInstance(id)
                .setPickupLocation(pickupPoint.loc())
                .setDeliveryLocation(deliveryPoint.loc())
                .addSizeDimension(0, sizeDimension);
    }

    public ShipmentJob setPickupTimeWindow(Date start, Date end) {
        this.builder.setPickupTimeWindow(TimeWindow.newInstance(start.getTime(),
                end.getTime()));
        System.out.println(start.getTime());
        System.out.println(end.getTime());
        return this;
    }

    public ShipmentJob setDeliveryTimeWindow(Date start, Date end) {
        this.builder.setDeliveryTimeWindow(TimeWindow.newInstance(start.getTime(),
                end.getTime()));
        return this;
    }

    @Override
    public Shipment build() {
        return this.builder.build();
    }

}
