package com.diditech.vrp.test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.diditech.vrp.JspritWrapper;
import com.diditech.vrp.geo.Point;
import com.diditech.vrp.job.ShipmentJob;
import com.diditech.vrp.utils.BaiduVehicleRoutingTransportCostsMatrix;
import com.diditech.vrp.vehicle.BaseVehicle;
import com.diditech.vrp.vehicle.FourSeatVehicleWithTimeWindow;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

public class MultiTest2 {

    private static List<Order> orderList = new ArrayList<Order>(){
        {add(new Order("小明", getMillisecond("08:30:00"), "120.399124,36.178779", "120.493266,36.155936"));}
        {add(new Order("小红", getMillisecond("12:30:00"), "120.42212,36.223048", "120.490535,36.156286"));}
        {add(new Order("小刚", getMillisecond("09:10:00"), "120.450147,36.111396", "120.379145,36.174118"));}
        {add(new Order("小亮", getMillisecond("16:40:00"), "120.423989,36.152556", "120.364773,36.086433"));}
    };

    private static int GLOBAL_INDEX = 2;

    public static void main(String[] args) throws UnsupportedEncodingException {
        long start = System.currentTimeMillis();
        Point v1Start = new Point(0, 120.421761, 36.119647);
        BaseVehicle fourSeatVehicle1 =
                new FourSeatVehicleWithTimeWindow("v1")
                .setStartLocation(v1Start)
                .setEarliestStart(getMillisecond("08:00:00"))
                .setLatestArrival(getMillisecond("17:00:00"))
                .setReturnToDepot(false);
        Point v2Start = new Point(1, 120.493554, 36.156635);
        BaseVehicle fourSeatVehicle2 =
                new FourSeatVehicleWithTimeWindow("v2")
                .setStartLocation(v2Start)
                .setEarliestStart(getMillisecond("08:00:00"))
                .setLatestArrival(getMillisecond("17:00:00"))
                .setReturnToDepot(false);
        JspritWrapper wrapper = JspritWrapper.create()
                .addVehicle(fourSeatVehicle1)
                .addVehicle(fourSeatVehicle2);

        for (Order order : orderList) {
            wrapper.addJob(createShipment(order.getName(), order.getTimestamp(),
                    order.getStart(), order.getEnd()));
        }

        wrapper.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        VehicleRoutingProblem problem = wrapper.buildProblem().getProblem();

        List<Location> list = problem.getAllLocations()
                .stream().sorted(Comparator.comparing(Location::getId)).collect(Collectors.toList());

        BaiduVehicleRoutingTransportCostsMatrix matrix =
                new BaiduVehicleRoutingTransportCostsMatrix(list, false);

        wrapper.setRoutingCost(matrix);
        // rebuild
        problem = wrapper.buildProblem().getProblem();

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

        /*
         * write out problem and solution to xml-file
         */
        new VrpXMLWriter(problem, solutions).write("output/shipment-problem-with-solution.xml");

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


    public static ShipmentJob createShipment(String name, Date date, String x1y1, String x2y2){
        String[] x1y1Arr = x1y1.split(",");
        String[] x2y2Arr = x2y2.split(",");
        Point point1 = new Point(GLOBAL_INDEX, Double.valueOf(x1y1Arr[0]), Double.valueOf(x1y1Arr[1]));
        GLOBAL_INDEX++;
        Point point2 = new Point(GLOBAL_INDEX, Double.valueOf(x2y2Arr[0]), Double.valueOf(x2y2Arr[1]));
        GLOBAL_INDEX++;
        ShipmentJob shipmentJob =
                new ShipmentJob(name, 1, point1, point2)
                //.setDeliveryTimeWindow()
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
        private String start;
        private String end;
    }

}
