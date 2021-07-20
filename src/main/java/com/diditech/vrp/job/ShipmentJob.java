package com.diditech.vrp.job;

import java.util.Date;

import com.diditech.vrp.IBuilder;
import com.diditech.vrp.IPoints;
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
public class ShipmentJob implements IBuilder, IPoints {

    protected Shipment.Builder builder;

    protected Point pickupPoint;

    protected Point deliveryPoint;

    public ShipmentJob(String id, int sizeDimension, Point pickupPoint, Point deliveryPoint) {
        this.pickupPoint = pickupPoint;
        this.deliveryPoint = deliveryPoint;
        this.builder = Shipment.Builder.newInstance(id)
                .setPickupLocation(pickupPoint.loc())
                .setDeliveryLocation(deliveryPoint.loc())
                .addSizeDimension(0, sizeDimension);
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
    public Point[] getPoints() {
        Point[] points = new Point[2];
        points[0] = pickupPoint;
        points[1] = deliveryPoint;
        return points;
    }

    @Override
    public Shipment build() {
        return this.builder.build();
    }

}
