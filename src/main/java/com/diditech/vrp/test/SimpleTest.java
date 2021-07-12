package com.diditech.vrp.test;

import static com.graphhopper.jsprit.core.problem.Location.newInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer.Label;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;


public class SimpleTest {

    private static final String baiduRoutingReq = "https://api.map.baidu.com/directionlite/v1/driving?origin=%s,%s&destination=%s,%s&ak=3lBS83HG7YYqCf31stUsYHISNCBb2c2a";


    public static void main(String[] args) {
        /*
         * some preparation - create output folder
         */
        File dir = new File("output");
        // if the directory does not exist, create it
        if (!dir.exists()) {
            System.out.println("creating directory ./output");
            boolean result = dir.mkdir();
            if (result) System.out.println("./output created");
        }

        /*
         * get a vehicle type-builder and build a type with the typeId "vehicleType" and one capacity dimension, i.e. weight, and capacity dimension value of 2
         */
        final int WEIGHT_INDEX = 0;
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(WEIGHT_INDEX, 1);
        VehicleType vehicleType = vehicleTypeBuilder.build();

        /*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
         */
        Builder vehicleBuilder = Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(newInstance(36.152549, 120.489742));
        vehicleBuilder.setType(vehicleType);
        vehicleBuilder.setReturnToDepot(false);
        VehicleImpl vehicle = vehicleBuilder.build();

        /*
         * build services at the required locations, each with a capacity-demand of 1.
         */
        Service service1 = Pickup.Builder.newInstance("1").addSizeDimension(WEIGHT_INDEX, 1).setLocation(newInstance(36.156993, 120.499292)).build();
        Service service2 = Pickup.Builder.newInstance("2").addSizeDimension(WEIGHT_INDEX, 1).setLocation(newInstance(36.158916, 120.480373)).build();

        Service service3 = Delivery.Builder.newInstance("3").addSizeDimension(WEIGHT_INDEX, 1).setLocation(newInstance(36.156643, 120.483535)).build();
        Service service4 = Delivery.Builder.newInstance("4").addSizeDimension(WEIGHT_INDEX, 1).setLocation(newInstance(36.145656, 120.491045)).build();


        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle);
        vrpBuilder.addJob(service1).addJob(service2).addJob(service3).addJob(service4);
        // 更换距离、时间算法 start
        // vrpBuilder.getLocations()
        VehicleRoutingTransportCostsMatrix costMatrix = createMatrix(vrpBuilder);
        vrpBuilder.setRoutingCost(costMatrix);
        // 更换距离、时间算法 end

        VehicleRoutingProblem problem = vrpBuilder.build();

        /*
         * get the algorithm out-of-the-box.
         */
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

        /*
         * and search a solution
         */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        /*
         * get the best
         */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        new VrpXMLWriter(problem, solutions).write("output/problem-with-solution.xml");

        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);

        /*
         * plot
         */
        new Plotter(problem, bestSolution).plot("output/plot.png", "simple example");

        /*
        render problem and solution with GraphStream
         */
        new GraphStreamViewer(problem, bestSolution).labelWith(Label.ID).setRenderDelay(200).display();
    }

    private static VehicleRoutingTransportCostsMatrix createMatrix(VehicleRoutingProblem.Builder vrpBuilder) {
        // FastVehicleRoutingTransportCostsMatrix
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        for (String from : vrpBuilder.getLocationMap().keySet()) {
            for (String to : vrpBuilder.getLocationMap().keySet()) {
                Coordinate fromCoord = vrpBuilder.getLocationMap().get(from);
                Coordinate toCoord = vrpBuilder.getLocationMap().get(to);
                // baidu
                String jsonString = HttpUtil.get(String.format(baiduRoutingReq, fromCoord.getX(), fromCoord.getY(), toCoord.getX(), toCoord.getY()));
                BaiduRoutingResult baiduRoutingResult = JSONUtil.toBean(jsonString, BaiduRoutingResult.class);
                double distance = baiduRoutingResult.getResult().getRoutes().get(0).getDistance();
                double duration = baiduRoutingResult.getResult().getRoutes().get(0).getDuration();
                matrixBuilder.addTransportDistance(from, to, distance);
                matrixBuilder.addTransportTime(from, to, duration);
            }
        }
        return matrixBuilder.build();
    }

    @Data
    public static class BaiduRoutingResult {
        private float status;
        private String message;
        private Result result;
    }

    @Data
    public static class Result {
        private Location origin;
        private Location destination;
        private List<Routes> routes = new ArrayList<>();
    }

    @Data
    public static class Location {
        private float lng;
        private float lat;
    }

    @Data
    public static class Routes {
        private double distance;
        private double duration;
        private float traffic_condition;
        private float toll;
        private List<Steps> steps = new ArrayList<>();
    }

    @Data
    public static class Steps {
        private float leg_index;
        private float distance;
        private float duration;
        private float direction;
        private float turn;
        private float road_type;
        private String road_types;
        private String instruction;
        private String path;
        private ArrayList<Object> traffic_condition = new ArrayList<>();
        private Location start_location;
        private Location end_location;
    }

}
