package com.diditech.vrp.vehicle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.diditech.vrp.IBuilder;
import com.diditech.vrp.IPoint;
import com.diditech.vrp.job.ShipmentJob;
import com.diditech.vrp.utils.Point;
import com.diditech.vrp.vehicle.type.BasicVehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Getter;

/**
 * 基础车辆
 *
 * @author hefan
 * @date 2021/7/14 13:47
 */
@Getter
public class BasicVehicle implements IBuilder, IPoint {

    protected BasicVehicleType vehicleType;

    protected VehicleImpl.Builder builder;

    protected String id;

    protected Point startPoint;

    protected Point endPoint;

    protected Set<String> skills = new HashSet<>();

    /**
     * 是否返回车库
     * 默认否
     */
    protected boolean returnToDepot = false;

    /**
     * 是否有结束
     */
    protected boolean hasEnd = false;

    /**
     * 初始作业
     */
    protected ShipmentJob initJob;

    public BasicVehicle(String id, BasicVehicleType vehicleType) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.builder = VehicleImpl.Builder.newInstance(id)
                .setType(this.vehicleType.build());
    }

    /**
     * 设置起始点
     */
    public void setStartLocation(Point point) {
        startPoint = point;
    }

    /**
     * 设置结束点
     */
    public void setEndLocation(Point point) {
        endPoint = point;
        hasEnd = true;
    }

    /**
     * 是否返回车库
     */
    public void setReturnToDepot(boolean returnToDepot) {
        this.returnToDepot = returnToDepot;
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

    /**
     * 设置初始单
     * @param job
     */
    public void setInitJob(ShipmentJob job){
        this.initJob = job;
    }

    @Override
    public VehicleImpl build() {
        fillPointId();
        this.builder.setStartLocation(startPoint.loc());
        this.builder.setReturnToDepot(returnToDepot);
        if(hasEnd) {
            this.builder.setStartLocation(endPoint.loc());
        }
        if(CollectionUtil.isNotEmpty(skills)){
            this.builder.addAllSkills(skills);
        }
        return this.builder.build();
    }

    @Override
    public List<Point> getPoints() {
        List<Point> points;
        if(hasEnd) {
            points = new ArrayList<>(2);
            points.add(startPoint);
            points.add(endPoint);
            return points;
        }
        points = new ArrayList<>(1);
        points.add(startPoint);
        return points;
    }

    @Override
    public String getPrefix() {
        return "V";
    }

}
