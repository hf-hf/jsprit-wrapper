package com.diditech.vrp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.diditech.vrp.exception.RepeatPointException;
import com.diditech.vrp.utils.Point;
import com.diditech.vrp.job.ShipmentJob;
import com.diditech.vrp.solution.VRPSolution;
import com.diditech.vrp.vehicle.BasicVehicle;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
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

    protected List<VRPSolution> vrpSolutions;

    protected List<BasicVehicle> vehicleList = new LinkedList<>();

    protected List<ShipmentJob> jobList = new LinkedList<>();

    protected Map<Integer, Point> pointMap = new HashMap<>();

    public static JspritWrapper create() {
        return new JspritWrapper();
    }

    public JspritWrapper addVehicle(BasicVehicle vehicle) {
        this.vehicleList.add(vehicle);
        this.builder.addVehicle(vehicle.build());
        return this;
    }

    public JspritWrapper setFleetSize(VehicleRoutingProblem.FleetSize fleetSize) {
        this.builder.setFleetSize(fleetSize);
        return this;
    }

    public JspritWrapper addJob(ShipmentJob job) {
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
        List<VRPSolution> vrpSolutions = new ArrayList<>(solutions.size());
        VRPSolution vrpSolution;
        for (VehicleRoutingProblemSolution solution : solutions) {
            vrpSolution = new VRPSolution(solution);
            vrpSolutions.add(vrpSolution);
        }
        this.vrpSolutions = vrpSolutions;
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

    public List<VRPSolution> getVRPSolutions() {
        return this.vrpSolutions;
    }

    public VRPSolution bestOf() {
        VRPSolution best = null;
        for (VRPSolution s : this.vrpSolutions) {
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

    /**
     * 确保point id不会有重复
     *
     * @param point
     */
    private void addPoint(Point point) {
        if (pointMap.containsKey(point.getId())) {
            throw new RepeatPointException(point);
        }
        pointMap.put(point.getId(), point);
    }

}
