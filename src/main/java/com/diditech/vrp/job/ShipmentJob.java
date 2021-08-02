package com.diditech.vrp.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.diditech.vrp.IBuilder;
import com.diditech.vrp.IPoint;
import com.diditech.vrp.JspritConfig;
import com.diditech.vrp.utils.Point;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import lombok.Getter;

/**
 * 装货作业
 *
 * @author hefan
 * @date 2021/7/8 14:37
 */
@Getter
public class ShipmentJob implements IBuilder, IPoint {

    protected Shipment.Builder builder;

    /**
     * 不重复的唯一标识
     */
    protected String id;

    /**
     * 起始点
     */
    protected Point pickupPoint;

    /**
     * 最终目的地
     */
    protected Point deliveryPoint;

    /**
     * 其他目的地
     */
    protected List<Point> wayPoints = new ArrayList<>();

    protected Set<String> skills = new HashSet<>();

    /**
     * 负载尺寸，如4座车对应每位乘客，每位乘车负载的尺寸为1
     */
    protected int sizeDimension;

    protected Date date;

    public ShipmentJob(String id, int sizeDimension, Point pickupPoint, Point deliveryPoint) {
        this.id = id;
        this.pickupPoint = pickupPoint;
        this.deliveryPoint = deliveryPoint;
        this.sizeDimension = sizeDimension;
        this.builder = Shipment.Builder.newInstance(id);
    }

    public ShipmentJob setDefaultTimeWindow(Date date) {
        return setPickupTimeWindow(date)
                .setDeliveryTimeWindow(date);
    }

    public ShipmentJob setPickupTimeWindow(Date date) {
        Date start = date;
        Date end = DateUtil.offsetMinute(date,
                JspritConfig.getInstance().getPickup_wait_minutes());
        return setPickupTimeWindow(start, end);
    }

    public ShipmentJob setPickupTimeWindow(Date start, Date end) {
        this.builder.setPickupTimeWindow(TimeWindow.newInstance(start.getTime(),
                end.getTime()));
        this.date = start;
        return this;
    }

    public ShipmentJob setDeliveryTimeWindow(Date start, Date end) {
        this.builder.setDeliveryTimeWindow(TimeWindow.newInstance(start.getTime(),
                end.getTime()));
        return this;
    }

    public ShipmentJob setDeliveryTimeWindow(Date date) {
        Date start = DateUtil.offsetMinute(date,
                JspritConfig.getInstance().getDelivery_wait_start_minutes());
        Date end = DateUtil.offsetMinute(date,
                JspritConfig.getInstance().getDelivery_wait_end_minutes());
        return setDeliveryTimeWindow(start, end);
    }

    public ShipmentJob addWayPoints(List<Point> points){
        wayPoints.addAll(points);
        return this;
    }


    /**
     * 加入skill
     */
    public void addSkill(String skill){
        this.skills.add(skill);
    }

    /**
     * 加入全部的skill
     */
    public void addAllSkills(Set<String> skills){
        this.skills.addAll(skills);
    }

    @Override
    public Shipment build() {
        fillPointId();
        this.builder
                .setPickupLocation(pickupPoint.loc())
                .setDeliveryLocation(deliveryPoint.loc())
                .addSizeDimension(0, sizeDimension);
        if(CollectionUtil.isNotEmpty(skills)){
            this.builder.addAllRequiredSkills(skills);
        }
        return this.builder.build();
    }

    @Override
    public List<Point> getPoints() {
        List<Point> points = new ArrayList<>(2);
        points.add(this.pickupPoint);
        points.add(this.deliveryPoint);
        return points;
    }

    @Override
    public String getPrefix() {
        return "J";
    }

}
