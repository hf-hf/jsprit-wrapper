package com.diditech.vrp.job;

import java.util.Date;

import com.diditech.vrp.IBuilder;
import com.diditech.vrp.utils.Point;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

import lombok.Getter;

/**
 * 装货作业
 *
 * @author hefan
 * @date 2021/7/8 14:37
 */
@Getter
public class ShipmentJob implements IBuilder {

    protected Shipment.Builder builder;

    protected String id;

    protected Point pickupPoint;

    protected Point deliveryPoint;

    /**
     * 负载尺寸，如4座车对应每位乘客，每位乘车负载的尺寸为1
     */
    protected int sizeDimension;

    public ShipmentJob(String id, int sizeDimension, Point pickupPoint, Point deliveryPoint) {
        this.id = id;
        this.pickupPoint = pickupPoint;
        this.deliveryPoint = deliveryPoint;
        this.sizeDimension = sizeDimension;
        this.builder = Shipment.Builder.newInstance(id);
    }

    public ShipmentJob setPickupTimeWindow(Date start, Date end) {
        this.builder.setPickupTimeWindow(TimeWindow.newInstance(start.getTime(),
                end.getTime()));
        return this;
    }

    public ShipmentJob setDeliveryTimeWindow(Date start, Date end) {
        this.builder.setDeliveryTimeWindow(TimeWindow.newInstance(start.getTime(),
                end.getTime()));
        return this;
    }

    @Override
    public Shipment build() {
        this.builder
                .setPickupLocation(pickupPoint.loc())
                .setDeliveryLocation(deliveryPoint.loc())
                .addSizeDimension(0, sizeDimension);
        return this.builder.build();
    }

}
