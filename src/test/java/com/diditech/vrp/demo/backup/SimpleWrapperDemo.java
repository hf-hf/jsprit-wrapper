package com.diditech.vrp.demo.backup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.diditech.vrp.JspritWrapper;
import com.diditech.vrp.job.ShipmentJob;
import com.diditech.vrp.solution.Problem;
import com.diditech.vrp.utils.Point;
import com.diditech.vrp.vehicle.BasicVehicle;
import com.diditech.vrp.vehicle.BasicVehicleWithTimeWindow;
import com.diditech.vrp.vehicle.type.BasicVehicleType;

import cn.hutool.core.date.DateUtil;

public class SimpleWrapperDemo {

    public static void main(String[] args) {
        // 说明是几座的车
        BasicVehicleType fourSeatVehicleType = new BasicVehicleType(4);
        // 设置车辆可作业时间窗口
        Date workStart = DateUtil.parseTimeToday("08:00:00");
        Date workEnd = DateUtil.parseTimeToday("17:00:00");
        BasicVehicleWithTimeWindow fourSeatVehicle1 =
                new BasicVehicleWithTimeWindow("v1", fourSeatVehicleType);
        fourSeatVehicle1.setStartLocation(new Point(120.421761, 36.119647));
        fourSeatVehicle1.setEarliestStart(workStart);
        fourSeatVehicle1.setLatestArrival(workEnd);
        fourSeatVehicle1.setReturnToDepot(false);

        BasicVehicleWithTimeWindow fourSeatVehicle2 =
                new BasicVehicleWithTimeWindow("v2", fourSeatVehicleType);
        fourSeatVehicle2.setStartLocation(new Point(120.493554, 36.156635));
        fourSeatVehicle2.setEarliestStart(workStart);
        fourSeatVehicle2.setLatestArrival(workEnd);
        fourSeatVehicle2.setReturnToDepot(false);

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
                // 10分钟内能够上车
                .setPickupTimeWindow(job1_start1, DateUtil.offsetMinute(job1_start1, 10))
                // 20~120分钟内能够送达目的地
                .setDeliveryTimeWindow(DateUtil.offsetMinute(job1_start1, 20),
                        DateUtil.offsetHour(job1_start1, 120));
        Date job1_start2 = DateUtil.parseTimeToday("12:30:00");
        ShipmentJob job1_2 = new ShipmentJob("小红", 1,
                new Point(120.42212, 36.223048),
                new Point(120.490535, 36.156286))
                .setPickupTimeWindow(job1_start2, DateUtil.offsetMinute(job1_start2, 10))
                .setDeliveryTimeWindow(DateUtil.offsetMinute(job1_start2, 20),
                        DateUtil.offsetHour(job1_start2, 120));
        jobList1.add(job1_1);
        jobList1.add(job1_2);

        List<ShipmentJob> jobList2 = new ArrayList<>();
        Date job2_start1 = DateUtil.parseTimeToday("08:45:00");
        ShipmentJob job2_1 = new ShipmentJob("小王", 1,
                new Point(120.399124, 36.178759),
                new Point(120.493266, 36.155966))
                .setPickupTimeWindow(job2_start1, DateUtil.offsetMinute(job2_start1, 10))
                .setDeliveryTimeWindow(DateUtil.offsetMinute(job2_start1, 20),
                        DateUtil.offsetHour(job2_start1, 120));
        jobList2.add(job2_1);

        List<ShipmentJob> jobList3 = new ArrayList<>();
        Date job3_start1 = DateUtil.parseTimeToday("16:30:00");
        ShipmentJob job3_1 = new ShipmentJob("小刘", 1,
                new Point(120.42212, 36.223028),
                new Point(120.490535, 36.156226))
                .setPickupTimeWindow(job3_start1, DateUtil.offsetMinute(job3_start1, 10))
                .setDeliveryTimeWindow(DateUtil.offsetMinute(job3_start1, 20),
                        DateUtil.offsetHour(job3_start1, 120));
        Date job3_start2 = DateUtil.parseTimeToday("09:30:00");
        ShipmentJob job3_2 = new ShipmentJob("小张", 1,
                new Point(120.399124, 36.178759),
                new Point(120.493266, 36.155466))
                .setPickupTimeWindow(job3_start2, DateUtil.offsetMinute(job3_start2, 10))
                .setDeliveryTimeWindow(DateUtil.offsetMinute(job3_start2, 20),
                        DateUtil.offsetHour(job3_start2, 120));
        jobList3.add(job3_1);
        jobList3.add(job3_2);

        // first round start
        long start = System.currentTimeMillis();

        Problem last = null;
        last = build(vehicleList, jobList1, last, null, null);
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
        last = build(vehicleList, jobList2, last, releasedJobId, releasedVehicleId);
        end = System.currentTimeMillis();
        System.out.println("second round total cost:" + (end - start));
        // second round end

        // third round start
        start = System.currentTimeMillis();
        last = build(vehicleList, jobList3, last, null, null);
        end = System.currentTimeMillis();
        System.out.println("third round total cost:" + (end - start));
        // third round end

        // fourth round start
        start = System.currentTimeMillis();
        List<ShipmentJob> jobList4 = new ArrayList<>();
        jobList4.add(job1_1);
        last = build(vehicleList, jobList4, last, null, null);
        end = System.currentTimeMillis();
        System.out.println("fourth round total cost:" + (end - start));
        // fourth round end
        //SolutionUtils.bestOf(last);
    }

    public static Problem build(List<BasicVehicle> vehicleList,
                                List<ShipmentJob> jobList, Problem lastProblem,
                                List<String> releasedJobId,
                                List<String> releasedVehicleId) {
        JspritWrapper wrapper = JspritWrapper.create(releasedVehicleId);
        Problem problem = wrapper
                .addInitialVehicleRoutes(lastProblem)
                //.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .addVehicles(vehicleList)
                .addJobs(jobList)
                .fastBuildProblem(true);
        wrapper.printBestSolution();
        return problem;
    }

}
