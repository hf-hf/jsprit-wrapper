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
import com.diditech.vrp.vehicle.BasicVehicleWithTimeWindow;
import com.diditech.vrp.vehicle.type.BasicVehicleType;

import cn.hutool.core.date.DateUtil;

public class SimpleWrapperDemo2 {

    public static void main(String[] args) {
        // 构造测试数据 start
        // 车辆容载量
        BasicVehicleType fourSeatVehicleType = new BasicVehicleType(4);

        BasicVehicleWithTimeWindow fourSeatVehicle1 =
                new BasicVehicleWithTimeWindow("v1", fourSeatVehicleType);
        fourSeatVehicle1.setStartLocation(Point.create(120.421761, 36.119647));

        BasicVehicleWithTimeWindow fourSeatVehicle2 =
                new BasicVehicleWithTimeWindow("v2", fourSeatVehicleType);
        fourSeatVehicle2.setStartLocation(Point.create(120.493554, 36.156635));
        // 设置车辆所属公司，仅能接该公司的单，可设置多个
        fourSeatVehicle2.addSkill("test");

        List<BasicVehicle> vehicleList = new ArrayList<>();
        vehicleList.add(fourSeatVehicle1);
        vehicleList.add(fourSeatVehicle2);

        List<ShipmentJob> jobList1 = new ArrayList<>();
        // 乘车人上车时间
        Date job1_start1 = DateUtil.parseTimeToday("08:30:00");
        // 乘车人选择到达时间或预计到达时间
        Date job1_end1 = DateUtil.parseTimeToday("10:30:00");
        ShipmentJob job1_1 = new ShipmentJob("小明",
                // 尺寸，这一次作业占用车辆容积的负载，如一个乘客占一个座
                1,
                // 出发地
                Point.create(120.399124, 36.178779),
                // 目的地
                Point.create(120.493266, 36.155936))
                .setPickupTimeWindow(job1_start1)
                .setDeliveryTimeWindow(job1_end1);
        Date job1_start2 = DateUtil.parseTimeToday("12:30:00");
        Date job1_end2 = DateUtil.parseTimeToday("12:30:00");
        ShipmentJob job1_2 = new ShipmentJob("小红", 1,
                Point.create(120.42212, 36.223048),
                Point.create(120.490535, 36.156286))
                .setPickupTimeWindow(job1_start2)
                .setDeliveryTimeWindow(job1_end2);

        List<Point> wayPoints = new ArrayList<>();
        wayPoints.add(Point.create(120.22212, 36.223348));
        wayPoints.add(Point.create(120.46212, 36.223778));
        // 若为多目的地，则其余目的地作为途经点加入job
        job1_1.addWayPoints(wayPoints);
        // 设置作业所属公司，该作业只会被分配到该公司的车辆下，对应车辆的skill
        job1_1.addSkill("test");
        jobList1.add(job1_1);
        jobList1.add(job1_2);

        List<ShipmentJob> jobList2 = new ArrayList<>();
        Date job2_start1 = DateUtil.parseTimeToday("08:45:00");
        Date job2_end1 = DateUtil.parseTimeToday("10:45:00");
        ShipmentJob job2_1 = new ShipmentJob("小王", 1,
                Point.create(120.399124, 36.178759),
                Point.create(120.493266, 36.155966))
                .setPickupTimeWindow(job2_start1)
                .setDeliveryTimeWindow(job2_end1);
        job2_1.addSkill("test");
        jobList2.add(job2_1);

        List<ShipmentJob> jobList3 = new ArrayList<>();
        Date job3_start1 = DateUtil.parseTimeToday("16:30:00");
        Date job3_end1 = DateUtil.parseTimeToday("18:30:00");
        ShipmentJob job3_1 = new ShipmentJob("小刘", 1,
                Point.create(120.42212, 36.223028),
                Point.create(120.490535, 36.156226))
                .setPickupTimeWindow(job3_start1)
                .setDeliveryTimeWindow(job3_end1);
        Date job3_start2 = DateUtil.parseTimeToday("09:30:00");
        Date job3_end2 = DateUtil.parseTimeToday("11:30:00");
        ShipmentJob job3_2 = new ShipmentJob("小张", 1,
                Point.create(120.399124, 36.178759),
                Point.create(120.493266, 36.155466))
                .setPickupTimeWindow(job3_start2)
                .setDeliveryTimeWindow(job3_end2);
        jobList3.add(job3_1);
        jobList3.add(job3_2);

        Date initStart = DateUtil.parseTimeToday("06:30:00");
        Date initEnd = DateUtil.parseTimeToday("08:30:00");
        ShipmentJob initJob = new ShipmentJob("小李", 1,
                Point.create(120.43212, 36.223028),
                Point.create(120.460535, 36.156226))
                .setPickupTimeWindow(initStart)
                .setDeliveryTimeWindow(initEnd);
        // 初始订单
        Map<String, ShipmentJob> initJobMap = new HashMap<>();
        initJobMap.put("v1", initJob);
        // 构造测试数据 end

        // 测试代码
        // zero round start
        JspritWrapper wrapper = JspritWrapper.create();
        Problem last = wrapper
                .addVehicles(vehicleList)
                .addInitialShipments(initJobMap)
                .fastBuildProblem();
        wrapper.printBestSolution();
        // zero round end

        // first round start
        long start = System.currentTimeMillis();
        wrapper = JspritWrapper.create();
        last = wrapper
                .addInitialVehicleRoutes(last)
                .addJobs(jobList1)
                .fastBuildProblem();
        wrapper.printBestSolution();
        long end = System.currentTimeMillis();
        System.out.println("first round total cost:" + (end - start));
        // first round start

        // second round start
        start = System.currentTimeMillis();
        // 需要释放的订单ID
        List<String> releasedJobId = new ArrayList<>();
        releasedJobId.add("小明");
        // 需要释放的车辆ID
        List<String> releasedVehicleId = new ArrayList<>();
        //releasedVehicleId.add("v1");
        wrapper = JspritWrapper.create(releasedJobId, releasedVehicleId);
        last = wrapper
                .addInitialVehicleRoutes(last)
                .addJobs(jobList2)
                .fastBuildProblem();
        wrapper.printBestSolution();
        end = System.currentTimeMillis();
        System.out.println("second round total cost:" + (end - start));
        // second round end

        // third round start
        start = System.currentTimeMillis();
        wrapper = JspritWrapper.create();
        last = wrapper
                .addInitialVehicleRoutes(last)
                .addJobs(jobList3)
                .fastBuildProblem();
        wrapper.printBestSolution();
        end = System.currentTimeMillis();
        System.out.println("third round total cost:" + (end - start));
        // third round end

        // fourth round start
        start = System.currentTimeMillis();
        List<ShipmentJob> jobList4 = new ArrayList<>();
        jobList4.add(job1_1);
        wrapper = JspritWrapper.create();
        last = wrapper
                .addInitialVehicleRoutes(last)
                .addJobs(jobList4)
                .fastBuildProblem();
        wrapper.printBestSolution();
        end = System.currentTimeMillis();
        System.out.println("fourth round total cost:" + (end - start));
        // fourth round end
        //SolutionUtils.bestOf(last);
    }

}
