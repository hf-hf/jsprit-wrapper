package com.diditech.vrp.demo.backup;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.diditech.vrp.remote.BaiduApi;
import com.diditech.vrp.remote.BaiduRouteMatrixResponse;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
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

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

public class OriginDemoTest {

    private static List<Order> orderList = new ArrayList<Order>(){
        {add(new Order("小明", getMillisecond("08:30:00"), "120.399124,36.178779", "120.493266,36.155936"));}
        {add(new Order("小红", getMillisecond("12:30:00"), "120.42212,36.223048", "120.490535,36.156286"));}
        //{add(new Order("小刚", getMillisecond("09:10:00"), "120.450147,36.111396", "120.379145,36.174118"));}
        //{add(new Order("小亮", getMillisecond("17:40:00"), "120.423989,36.152556", "120.364773,36.086433"));}
    };

    private static int GLOBAL_INDEX = 0;

    public static void main(String[] args) throws UnsupportedEncodingException {
        long start = System.currentTimeMillis();
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

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle1).addVehicle(vehicle2);

        for (Order order : orderList) {
            Shipment shipment = createShipment(order.getName(), order.getTimestamp(),
                    order.getStart(), order.getEnd());
            vrpBuilder.addJob(shipment);
        }
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        VehicleRoutingProblem problem = vrpBuilder.build();

        List<Location> list = problem.getAllLocations().stream().sorted(Comparator.comparing(Location::getId)).collect(Collectors.toList());

        FastVehicleRoutingTransportCostsMatrix matrix =
                createMatrix(list.size(), BaiduApi.routeMatrix(list));
        vrpBuilder.setRoutingCost(matrix);

        double[][][] tmp = matrix.getMatrix();

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

        System.out.println("total:" + (end - start));
        VehicleRoutingTransportCosts costs = problem.getTransportCosts();

        /*
         * write out problem and solution to xml-file
         */
        new VrpXMLWriter(problem, solutions).write("output/shipment-problem-with-solution.xml");

//        Problem problem1 = new VrpResultWriter().write(problem, solutions, true);
//        System.out.println(JSON.toJSONString(problem1));
//
//        List<VehicleRoutingProblemSolution> solutions1 =
//                JSONUtil.toList(jsonObj.getJSONArray("solutions"),
//                VehicleRoutingProblemSolution.class);

        /*
         * print nRoutes and totalCosts of bestSolution
         */
        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);

        /*
         * plot
         */
        new Plotter(problem, bestSolution).plot("output/plot.png", "simple example");

        new GraphStreamViewer(problem, Solutions.bestOf(solutions)).labelWith(GraphStreamViewer.Label.ACTIVITY)
                .setGraphStreamFrameScalingFactor(2)
                .setRenderDelay(2000)
                .setRenderShipments(true)
                //.setCameraView(30, 30, 1)
                .display();

    }



    private static FastVehicleRoutingTransportCostsMatrix createMatrix(int size, BaiduRouteMatrixResponse response){
        FastVehicleRoutingTransportCostsMatrix.Builder builder = FastVehicleRoutingTransportCostsMatrix.Builder
                .newInstance(size, false);

        List<BaiduRouteMatrixResponse.ResultBean> result = response.getResult();
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
