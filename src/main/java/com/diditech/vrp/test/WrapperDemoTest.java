package com.diditech.vrp.test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.diditech.vrp.JspritWrapper;
import com.diditech.vrp.utils.Point;
import com.diditech.vrp.job.ShipmentJob;
import com.diditech.vrp.solution.VrpSolution;
import com.diditech.vrp.utils.BaiduVehicleRoutingTransportCostsMatrix;
import com.diditech.vrp.vehicle.FourSeatVehicleWithTimeWindow;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

public class WrapperDemoTest {

    private static List<Order> orderList = new ArrayList<Order>(){
        {add(new Order("小明", getMillisecond("08:30:00"), 2, 3));}
        {add(new Order("小红", getMillisecond("12:30:00"), 4, 5));}
        {add(new Order("小刚", getMillisecond("09:10:00"), 6, 7));}
        {add(new Order("小亮", getMillisecond("17:40:00"), 8, 9));}
    };

    private static List<Point> pointList = new ArrayList<Point>(){
        {add(new Point(0, 120.421761, 36.119647));}
        {add(new Point(1, 120.493554, 36.156635));}
        {add(new Point(2, 120.399124,36.178779));}
        {add(new Point(3, 120.493266,36.155936));}
        {add(new Point(4, 120.42212,36.223048));}
        {add(new Point(5, 120.490535,36.156286));}
        {add(new Point(6, 120.450147,36.111396));}
        {add(new Point(7, 120.379145,36.174118));}
        {add(new Point(8, 120.423989,36.152556));}
        {add(new Point(9, 120.364773,36.086433));}
    };

    public static void main(String[] args) throws UnsupportedEncodingException {
        long start = System.currentTimeMillis();
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

        JspritWrapper wrapper = JspritWrapper.create()
                .addVehicle(fourSeatVehicle1)
                .addVehicle(fourSeatVehicle2);

        for (Order order : orderList) {
            wrapper.addJob(createShipment(order.getName(), order.getTimestamp(),
                    order.getStart(), order.getEnd()));
        }

        wrapper.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        Map<String, Coordinate> locationMap = wrapper.getLocationMap();

        BaiduVehicleRoutingTransportCostsMatrix matrix =
                new BaiduVehicleRoutingTransportCostsMatrix(locationMap, false);
        wrapper.setRoutingCost(matrix);

        VehicleRoutingProblem problem = null;//wrapper.buildProblem().getProblem(false);

        /*
         * get the algorithm out-of-the-box.
         */
        VehicleRoutingAlgorithm algorithm = wrapper.createAlgorithm().getAlgorithm();
//		algorithm.setMaxIterations(30000);
        /*
         * and search a solution
         */
        Collection<VehicleRoutingProblemSolution> solutions = wrapper.searchSolutions().getSolutions();

        List<VrpSolution> vrpList = wrapper.getVRPSolutions();
        System.out.println(JSON.toJSONString(vrpList));

        /*
         * get the best
         */
        VehicleRoutingProblemSolution bestSolution = wrapper.bestOfSolutions();

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


    public static ShipmentJob createShipment(String name, Date date, int start, int end){
        Point point1 = pointList.get(start);
        Point point2 = pointList.get(end);
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
        private int start;
        private int end;
    }

}
