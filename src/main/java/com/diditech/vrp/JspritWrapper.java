package com.diditech.vrp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.diditech.vrp.domain.Problem;
import com.diditech.vrp.exception.EmptyPointException;
import com.diditech.vrp.exception.RepeatPointException;
import com.diditech.vrp.job.ShipmentJob;
import com.diditech.vrp.solution.VrpSolution;
import com.diditech.vrp.utils.Point;
import com.diditech.vrp.utils.VrpJsonReader;
import com.diditech.vrp.utils.VrpJsonWriter;
import com.diditech.vrp.vehicle.BasicVehicle;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;

/**
 * Jsprit包装器
 *
 * @author hefan
 * @date 2021/7/8 15:11
 */
public class JspritWrapper {

    protected VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

    protected VehicleRoutingProblem problem;

    protected VehicleRoutingAlgorithm algorithm;

    protected Collection<VehicleRoutingProblemSolution> solutions;

    protected List<VrpSolution> vrpSolutions;

    protected Map<String, VehicleImpl> vehicleMap = new HashMap<>();

    protected List<ShipmentJob> jobList = new LinkedList<>();

    protected Map<Integer, Point> pointMap = new HashMap<>();

    public static JspritWrapper create() {
        return new JspritWrapper();
    }

    public JspritWrapper addInitialVehicleRoutes(Problem lastProblem) {
        // 加载之前的单记录
        VrpJsonReader reader = new VrpJsonReader(this.builder, lastProblem);
        reader.read();
        this.builder.addInitialVehicleRoutes(reader.getRoutes());
        return this;
    }

    public JspritWrapper addVehicle(BasicVehicle vehicle) {
        checkPoint(vehicle);
        VehicleImpl vehicleImpl = vehicle.build();
        this.vehicleMap.put(vehicle.getId(), vehicleImpl);
        this.builder.addVehicle(vehicleImpl);
        return this;
    }

    public JspritWrapper setFleetSize(VehicleRoutingProblem.FleetSize fleetSize) {
        this.builder.setFleetSize(fleetSize);
        return this;
    }

    public JspritWrapper addJob(ShipmentJob job) {
        checkPoint(job);
        this.jobList.add(job);
        this.builder.addJob(job.build());
        return this;
    }

    public JspritWrapper setRoutingCost(VehicleRoutingTransportCosts routingCost) {
        this.builder.setRoutingCost(routingCost);
        return this;
    }

    public JspritWrapper buildProblem() {
        this.problem = this.builder.build();
        return this;
    }

    public JspritWrapper createAlgorithm() {
        this.algorithm = Jsprit.createAlgorithm(this.problem);
        return this;
    }

    public JspritWrapper searchSolutions() {
        this.solutions = this.algorithm.searchSolutions();
        List<VrpSolution> vrpSolutions = new ArrayList<>(solutions.size());
        VrpSolution vrpSolution;
        for (VehicleRoutingProblemSolution solution : solutions) {
            vrpSolution = new VrpSolution(solution);
            vrpSolutions.add(vrpSolution);
        }
        this.vrpSolutions = vrpSolutions;
        // load shipment
        return this;
    }

    public Collection<VehicleRoutingProblemSolution> getSolutions() {
        return this.solutions;
    }

    public VehicleRoutingAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    public VehicleRoutingProblem getProblem() {
        return this.problem;
    }

    public Problem getProblem(boolean onlyBestSolution) {
        return new VrpJsonWriter().write(this.problem, this.solutions, onlyBestSolution);
    }

    public List<VrpSolution> getVRPSolutions() {
        return this.vrpSolutions;
    }

    public VrpSolution bestOf() {
        VrpSolution best = null;
        for (VrpSolution s : this.vrpSolutions) {
            if (best == null) best = s;
            else if (s.getCost() < best.getCost()) best = s;
        }
        return best;
    }

    public Map<String, Coordinate> getLocationMap() {
        return this.builder.getLocationMap();
    }

    public VehicleRoutingProblemSolution bestOfSolutions() {
        return Solutions.bestOf(this.solutions);
    }

    public void print(){
        SolutionPrinter.print(this.problem, bestOfSolutions(), SolutionPrinter.Print.VERBOSE);
    }

    /**
     * 确保point id不会有重复
     *
     * @param iPoints
     */
    private void checkPoint(IPoints iPoints) {
        Point[] points = iPoints.getPoints();
        if (points.length == 0) {
            return;
        }
        for (Point point : points) {
            if (null == point) {
                throw new EmptyPointException();
            }
            if (pointMap.containsKey(point.getId())) {
                throw new RepeatPointException(point);
            }
            pointMap.put(point.getId(), point);
        }
    }

}
