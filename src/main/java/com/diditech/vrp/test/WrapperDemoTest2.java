package com.diditech.vrp.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.diditech.vrp.JspritWrapper;
import com.diditech.vrp.domain.Problem;
import com.diditech.vrp.job.ShipmentJob;
import com.diditech.vrp.utils.BaiduVehicleRoutingTransportCostsMatrix;
import com.diditech.vrp.utils.Point;
import com.diditech.vrp.vehicle.BasicVehicle;
import com.diditech.vrp.vehicle.FourSeatVehicleWithTimeWindow;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.util.Coordinate;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

public class WrapperDemoTest2 {

    private static List<Order> orderList_0 = new ArrayList<Order>(){
        {add(new Order("小明", getMillisecond("08:30:00"), 2, 3));}
        {add(new Order("小红", getMillisecond("12:30:00"), 4, 5));}
    };

    private static List<Order> orderList_1 = new ArrayList<Order>(){
        {add(new Order("小王", getMillisecond("08:30:00"), 6, 7));}
    };

    private static List<Order> orderList_2 = new ArrayList<Order>(){
        {add(new Order("小刘", getMillisecond("16:30:00"), 8, 9));}
        {add(new Order("小张", getMillisecond("09:30:00"), 10, 11));}
    };

    private static List<Point> pointList = new ArrayList<Point>(){
        {add(new Point(0, 120.421761, 36.119647));}
        {add(new Point(1, 120.493554, 36.156635));}

        {add(new Point(2, 120.399124,36.178779));}
        {add(new Point(3, 120.493266,36.155936));}
        {add(new Point(4, 120.42212,36.223048));}
        {add(new Point(5, 120.490535,36.156286));}
        {add(new Point(6, 120.399124,36.178759));}
        {add(new Point(7, 120.493266,36.155966));}
        {add(new Point(8, 120.42212,36.223028));}
        {add(new Point(9, 120.490535,36.156226));}

        {add(new Point(10, 120.399124,36.178759));}
        {add(new Point(11, 120.493266,36.155466));}
    };

    public static Problem run(List<BasicVehicle> vehicleList, List<Order> orderList,
                              Problem lastProblem){
        long start = System.currentTimeMillis();
        JspritWrapper wrapper = JspritWrapper.create();
        if(null != lastProblem){
            wrapper.addInitialVehicleRoutes(lastProblem);
        }
        if(CollectionUtil.isNotEmpty(vehicleList)){
            for (BasicVehicle vehicle : vehicleList) {
                wrapper.addVehicle(vehicle);
            }
        }
        for (Order order : orderList) {
            wrapper.addJob(createShipment(order.getName(), order.getTimestamp(),
                    order.getStart(), order.getEnd()));
        }

        wrapper.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        Map<String, Coordinate> locationMap = wrapper.getLocationMap();
        BaiduVehicleRoutingTransportCostsMatrix matrix =
                new BaiduVehicleRoutingTransportCostsMatrix(locationMap, false);
        wrapper.setRoutingCost(matrix);
        /*
         * and search a solution
         */
        Problem problem = wrapper.buildProblem()
                .createAlgorithm().searchSolutions().getProblem(false);
        long end = System.currentTimeMillis();
        System.out.println("total:" + (end - start));
        wrapper.print();
        return problem;
    }


    public static void main(String[] args) {
        FourSeatVehicleWithTimeWindow fourSeatVehicle1 =
                new FourSeatVehicleWithTimeWindow("v1");
        fourSeatVehicle1.setStartLocation(pointList.get(0));
        fourSeatVehicle1.setEarliestStart(getMillisecond("08:00:00"));
        fourSeatVehicle1.setLatestArrival(getMillisecond("17:00:00"));
        fourSeatVehicle1.setReturnToDepot(false);

        FourSeatVehicleWithTimeWindow fourSeatVehicle2 =
                new FourSeatVehicleWithTimeWindow("v2");
        fourSeatVehicle2.setStartLocation(pointList.get(1));
        fourSeatVehicle2.setEarliestStart(getMillisecond("08:00:00"));
        fourSeatVehicle2.setLatestArrival(getMillisecond("17:00:00"));
        fourSeatVehicle2.setReturnToDepot(false);

        List<BasicVehicle> vehicleList = new ArrayList<>();
        vehicleList.add(fourSeatVehicle1);
        vehicleList.add(fourSeatVehicle2);

        Problem last = null;
        List<Order> list;
        for(int i=0;i <= 2;i++){
            if(i == 0){
                list = orderList_0;
            } else if(i == 1) {
                list = orderList_1;
            } else {
                list = orderList_2;
            }
            last = run(vehicleList, list, last);
            vehicleList = null;
        }
    }


    public static ShipmentJob createShipment(String name, Date date, int start, int end){
        Point point1 = pointList.get(start);
        Point point2 = pointList.get(end);
        ShipmentJob shipmentJob =
                new ShipmentJob(name, 1, point1, point2)
                // 1~2小时内送达目的地
                .setDeliveryTimeWindow(DateUtil.offsetHour(date, 1), DateUtil.offsetHour(date, 2))
                .setPickupTimeWindow(date, DateUtil.offsetMinute(date, 10));
        return shipmentJob;
    }

    public static Date getMillisecond(String timeString){
        return DateUtil.parseDateTime("2021-07-06 " + timeString);
    }

    @Data
    @AllArgsConstructor
    public static class Order {
        private String name;
        private Date timestamp;
        private int start;
        private int end;
    }

}
