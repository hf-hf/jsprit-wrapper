package com.diditech.vrp.demo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diditech.vrp.JspritWrapper;
import com.diditech.vrp.job.ShipmentJob;
import com.diditech.vrp.solution.Problem;
import com.diditech.vrp.utils.Point;
import com.diditech.vrp.vehicle.BasicVehicle;
import com.diditech.vrp.vehicle.FourSeatVehicleWithTimeWindow;

import cn.hutool.core.date.DateUtil;

public class SimpleWrapperDemo2 {

    public static void main(String[] args) {
        // 说明是几座的车
        FourSeatVehicleWithTimeWindow fourSeatVehicle1 = new FourSeatVehicleWithTimeWindow("v1");
        fourSeatVehicle1.setStartLocation(Point.create(120.421761, 36.119647));

        FourSeatVehicleWithTimeWindow fourSeatVehicle2 = new FourSeatVehicleWithTimeWindow("v2");
        fourSeatVehicle2.setStartLocation(Point.create(120.493554, 36.156635));

        List<BasicVehicle> vehicleList = new ArrayList<>();
        vehicleList.add(fourSeatVehicle1);
        vehicleList.add(fourSeatVehicle2);

        List<ShipmentJob> jobList1 = new ArrayList<>();
        Date job1_start1 = DateUtil.parseTimeToday("08:30:00");
        ShipmentJob job1_1 = new ShipmentJob("小明",
                // 尺寸，这一次作业占用车辆容积的负载，比如一个乘客占一个座
                1,
                // 出发地
                new Point(120.399124, 36.178779),
                // 目的地
                new Point(120.493266, 36.155936))
                .setDefaultTimeWindow(job1_start1);
        Date job1_start2 = DateUtil.parseTimeToday("12:30:00");
        ShipmentJob job1_2 = new ShipmentJob("小红", 1,
                new Point(120.42212, 36.223048),
                new Point(120.490535, 36.156286))
                .setDefaultTimeWindow(job1_start2);
        jobList1.add(job1_1);
        jobList1.add(job1_2);

        List<ShipmentJob> jobList2 = new ArrayList<>();
        Date job2_start1 = DateUtil.parseTimeToday("08:45:00");
        ShipmentJob job2_1 = new ShipmentJob("小王", 1,
                new Point(120.399124, 36.178759),
                new Point(120.493266, 36.155966))
                .setPickupTimeWindow(job2_start1)
                .setDeliveryTimeWindow(job2_start1);
        jobList2.add(job2_1);

        List<ShipmentJob> jobList3 = new ArrayList<>();
        Date job3_start1 = DateUtil.parseTimeToday("16:30:00");
        ShipmentJob job3_1 = new ShipmentJob("小刘", 1,
                new Point(120.42212, 36.223028),
                new Point(120.490535, 36.156226))
                .setDefaultTimeWindow(job3_start1);
        Date job3_start2 = DateUtil.parseTimeToday("09:30:00");
        ShipmentJob job3_2 = new ShipmentJob("小张", 1,
                new Point(120.399124, 36.178759),
                new Point(120.493266, 36.155466))
                .setDefaultTimeWindow(job3_start2);
        jobList3.add(job3_1);
        jobList3.add(job3_2);

        // zero round start
        Date initStart = DateUtil.parseTimeToday("06:30:00");
        ShipmentJob initJob = new ShipmentJob("小李", 1,
                new Point(120.43212, 36.223028),
                new Point(120.460535, 36.156226))
                .setDefaultTimeWindow(initStart);
        Map<String, ShipmentJob> initJobMap = new HashMap<>();
        initJobMap.put("v1", initJob);
        Problem last = null;
        last = build(vehicleList, null, last, null, null, initJobMap);
        vehicleList = null;
        // zero round end

        // first round start
        long start = System.currentTimeMillis();

        last = null;
        last = build(vehicleList, jobList1, last, null, null, null);
        vehicleList = null;

        long end = System.currentTimeMillis();
        System.out.println("first round total cost:" + (end - start));
        // first round start

        // second round start
        start = System.currentTimeMillis();
        List<String> releasedJobId = new ArrayList<>();
        releasedJobId.add("小明");
        List<String> releasedVehicleId = new ArrayList<>();
        releasedVehicleId.add("v1");
        last = build(vehicleList, jobList2, last, releasedJobId, releasedVehicleId, null);
        end = System.currentTimeMillis();
        System.out.println("second round total cost:" + (end - start));
        // second round end

        // third round start
        start = System.currentTimeMillis();
        last = build(vehicleList, jobList3, last, null, null, null);
        end = System.currentTimeMillis();
        System.out.println("third round total cost:" + (end - start));
        // third round end

        // fourth round start
        start = System.currentTimeMillis();
        List<ShipmentJob> jobList4 = new ArrayList<>();
        jobList4.add(job1_1);
        last = build(vehicleList, jobList4, last, null, null, null);
        end = System.currentTimeMillis();
        System.out.println("fourth round total cost:" + (end - start));
        // fourth round end
        //SolutionUtils.bestOf(last);
    }

    public static Problem build(List<BasicVehicle> vehicleList,
                                List<ShipmentJob> jobList, Problem lastProblem,
                                List<String> releasedJobId,
                                List<String> releasedVehicleId,
                                Map<String, ShipmentJob> initJobMap) {
        JspritWrapper wrapper = JspritWrapper.create(releasedJobId, releasedVehicleId);
        Problem problem = wrapper
                .addInitialVehicleRoutes(lastProblem)
                //.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .addVehicles(vehicleList)
                .addInitialShipments(initJobMap)
                .addJobs(jobList)
                .setDefaultBaiduRoutingCost()
                .buildProblem(true);
        wrapper.printBestSolution();
        return problem;
    }

}
