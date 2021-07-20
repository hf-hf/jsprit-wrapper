package com.diditech.vrp.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.diditech.vrp.baidu.BaiduResponse;
import com.diditech.vrp.domain.Problem;
import com.diditech.vrp.utils.SingleCoordinateBaiduVehicleRoutingTransportCostsMatrix;
import com.diditech.vrp.utils.VrpJsonReader;
import com.diditech.vrp.utils.VrpJsonWriter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

public class OriginDemoTest2 {

    private static List<Order> orderList_0 = new ArrayList<Order>(){
        {add(new Order("小明", getMillisecond("08:30:00"), "120.399124,36.178779", "120.493266,36.155936"));}
        {add(new Order("小红", getMillisecond("12:30:00"), "120.42212,36.223048", "120.490535,36.156286"));}
    };

    private static List<Order> orderList_1 = new ArrayList<Order>(){
        {add(new Order("小王", getMillisecond("08:30:00"), "120.399124,36.178759", "120.493266,36.155966"));}
    };

    private static List<Order> orderList_2 = new ArrayList<Order>(){
        {add(new Order("小刘", getMillisecond("17:30:00"), "120.42212,36.223028", "120.490535,36.156226"));}
    };

    private static int GLOBAL_INDEX = 0;

    public static void main(String[] args) {
        // 4座车类型
        // 提取车辆类型类
        VehicleTypeImpl.Builder vehicleTypeBuilder =
                VehicleTypeImpl.Builder.newInstance("vehicleType")
                        .addCapacityDimension(0, 4);
        VehicleType vehicleType = vehicleTypeBuilder.build();
        // 创建车辆对象
        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("v1");
        // 工作时间使用
        vehicleBuilder.setEarliestStart(getMillisecond("08:00:00"));
        vehicleBuilder.setLatestArrival(getMillisecond("17:00:00"));
        vehicleBuilder.setReturnToDepot(false);
        vehicleBuilder.setStartLocation(loc(Coordinate.newInstance(120.421761, 36.119647)));
        vehicleBuilder.setType(vehicleType);
        VehicleImpl vehicle1 = vehicleBuilder.build();

        // 创建车辆对象
        vehicleBuilder = VehicleImpl.Builder.newInstance("v2");
        // 工作时间使用
        vehicleBuilder.setEarliestStart(getMillisecond("08:00:00"));
        vehicleBuilder.setLatestArrival(getMillisecond("17:00:00"));
        vehicleBuilder.setReturnToDepot(false);
        vehicleBuilder.setStartLocation(loc(Coordinate.newInstance(120.493554, 36.156635)));
        vehicleBuilder.setType(vehicleType);
        VehicleImpl vehicle2 = vehicleBuilder.build();

        List<VehicleImpl> vehicleList = new ArrayList<>(2);
        vehicleList.add(vehicle1);
        vehicleList.add(vehicle2);

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

    private static Problem run(List<VehicleImpl> vehicleList, List<Order> orderList,
                               Problem last){
        long start = System.currentTimeMillis();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        if(CollectionUtil.isNotEmpty(vehicleList)){
            vrpBuilder.addAllVehicles(vehicleList);
        }
        if(null != last){
            // 加载之前的单记录
            VrpJsonReader reader = new VrpJsonReader(vrpBuilder, last);
            reader.read();
            vrpBuilder.addInitialVehicleRoutes(reader.getRoutes());
        }

        for (Order order : orderList) {
            Shipment shipment = createShipment(order.getName(), order.getTimestamp(),
                    order.getStart(), order.getEnd());
            vrpBuilder.addJob(shipment);
        }
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        VehicleRoutingProblem problem = vrpBuilder.build();

        List<Location> list = problem.getAllLocations().stream()
                //.filter(loc -> Integer.parseInt(loc.getId()) >= 10)
                .sorted(Comparator.comparing(Location::getId)).collect(Collectors.toList());


//        VehicleRoutingTransportCostsMatrix.Builder builder2 =
//                VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
//        //builder2.addTransportDistance()
//        builder2.

        SingleCoordinateBaiduVehicleRoutingTransportCostsMatrix matrix =
                new SingleCoordinateBaiduVehicleRoutingTransportCostsMatrix(vrpBuilder.getLocationMap(), false);
        vrpBuilder.setRoutingCost(matrix);

        //matrix.getMatrix();
        problem = vrpBuilder.build();

        /*
         * get the algorithm out-of-the-box.
         */
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
//		algorithm.setMaxIterations(30000);
        /*
         * and search a solution
         */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        /*
         * get the best
         */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        long end = System.currentTimeMillis();

        //System.out.println("total:" + (end - start));

        /*
         * write out problem and solution to xml-file
         */
        new VrpXMLWriter(problem, solutions).write("output/shipment-problem-with-solution.xml");

        Problem problem1 = new VrpJsonWriter().write(problem, solutions, true);
        System.out.println(JSON.toJSONString(problem1));

        /*
         * print nRoutes and totalCosts of bestSolution
         */
        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);
        return problem1;

        /*
         * plot
         */
//        new Plotter(problem, bestSolution).plot("output/plot.png", "simple example");
//
//        new GraphStreamViewer(problem, Solutions.bestOf(solutions)).labelWith(GraphStreamViewer.Label.ACTIVITY)
//                .setGraphStreamFrameScalingFactor(2)
//                .setRenderDelay(2000)
//                .setRenderShipments(true)
//                //.setCameraView(30, 30, 1)
//                .display();
    }



    private static FastVehicleRoutingTransportCostsMatrix createMatrix(int size, BaiduResponse response){
        FastVehicleRoutingTransportCostsMatrix.Builder builder = FastVehicleRoutingTransportCostsMatrix.Builder
                .newInstance(size, false);

        List<BaiduResponse.ResultBean> result = response.getResult();
        int index = 0;
        for(int i=0;i < size;i++){
            for(int j=0;j < size;j++){
                double distance = result.get(index).getDistance().getValue();
                double duration = result.get(index).getDuration().getValue() * 1000;
                builder.addTransportTimeAndDistance(i, j, duration, distance);
                index++;
            }
        }

//        list.parallelStream().forEach(loc1 -> {
//            list.stream().forEach(loc2 -> {
//                Coordinate xc = loc1.getCoordinate();
//                Coordinate yc = loc2.getCoordinate();
//                String jsonString = HttpUtil.get(String.format(baiduRoutingReq,
//                        xc.getY(), xc.getX(), yc.getY(), yc.getX()));
//                BaiduRoutingResult baiduRoutingResult = JSONUtil.toBean(jsonString, BaiduRoutingResult.class);
//                double distance = baiduRoutingResult.getResult().getRoutes().get(0).getDistance();
//                double duration = baiduRoutingResult.getResult().getRoutes().get(0).getDuration();
//                builder.addTransportTimeAndDistance(loc1.getIndex(), loc2.getIndex(), duration * 1000, distance);
//                System.out.println("finish " + loc1.getIndex() + "-" + loc2.getIndex());
//            });
//        });
//        for (Location loc1 : list) {
//            for (Location loc2 : list) {
//                Coordinate xc = loc1.getCoordinate();
//                Coordinate yc = loc2.getCoordinate();
//                String jsonString = HttpUtil.get(String.format(baiduRoutingReq,
//                        xc.getY(), xc.getX(), yc.getY(), yc.getX()));
//                BaiduRoutingResult baiduRoutingResult = JSONUtil.toBean(jsonString, BaiduRoutingResult.class);
//                double distance = baiduRoutingResult.getResult().getRoutes().get(0).getDistance();
//                double duration = baiduRoutingResult.getResult().getRoutes().get(0).getDuration();
//                builder.addTransportTimeAndDistance(loc1.getIndex(), loc2.getIndex(), duration * 1000, distance);
//                System.out.println("finish " + loc1.getIndex() + "-" + loc2.getIndex());
//            }
//        }
        return builder.build();
    }

    private static Location loc(Coordinate coordinate) {
        int index = GLOBAL_INDEX++;
        return Location.Builder.newInstance()
                .setCoordinate(coordinate)
                .setIndex(index)
                .setId(index + "")
                .build();
    }

    private static Coordinate createCoordinate(String x, String y){
        return Coordinate.newInstance(Double.parseDouble(x), Double.parseDouble(y));
    }

    public static Shipment createShipment(String name, long startTime, String x1y1, String x2y2){
        String[] x1y1Arr = x1y1.split(",");
        String[] x2y2Arr = x2y2.split(",");
        Shipment shipment = Shipment.Builder.newInstance(name)
                // 上车时限 10分钟
                .setPickupTimeWindow(TimeWindow.newInstance(startTime, startTime + 10 * 60 * 1000))
                .setDeliveryTimeWindow(TimeWindow.newInstance(startTime + 60 * 60 * 1000, startTime + 2 * 60 * 60 * 1000))
                // 暂定人数每次都为1人
                .addSizeDimension(0, 1)
                .setPickupLocation(loc(createCoordinate(x1y1Arr[0], x1y1Arr[1])))
                .setDeliveryLocation(loc(createCoordinate(x2y2Arr[0], x2y2Arr[1])))
                .build();
        return shipment;
    }

    public static long getMillisecond(String timeString){
        return DateUtil.parseDateTime("2021-07-06 " + timeString).getTime();
    }

    @Data
    public static class BaiduRoutingResult {
        private float status;
        private String message;
        private SimpleTest.Result result;
    }

    @Data
    @AllArgsConstructor
    public static class Order {
        private String name;
        private long timestamp;
        private String start;
        private String end;
    }

}
