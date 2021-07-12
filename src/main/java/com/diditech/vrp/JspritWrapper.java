package com.diditech.vrp;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.diditech.vrp.geo.Point;
import com.diditech.vrp.job.ShipmentJob;
import com.diditech.vrp.vehicle.BaseVehicle;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.util.Solutions;

/**
 * Jsprit包装器
 * @author hefan
 * @date 2021/7/8 15:11
 */
public class JspritWrapper {

    private VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

    private VehicleRoutingProblem problem;

    private VehicleRoutingAlgorithm algorithm;

    private Collection<VehicleRoutingProblemSolution> solutions;

    private List<BaseVehicle> vehicleList = new LinkedList<>();

    private List<ShipmentJob> jobList = new LinkedList<>();

    // 确保point id不会有重复
    private List<Point> pointList = new LinkedList<>();

    public static JspritWrapper create() {
        return new JspritWrapper();
    }

    public JspritWrapper addVehicle(BaseVehicle vehicle) {
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

    public JspritWrapper setRoutingCost(VehicleRoutingTransportCosts routingCost){
        this.builder.setRoutingCost(routingCost);
        return this;
    }

    public JspritWrapper buildProblem() {
        this.problem = this.builder.build();
        return this;
    }

    public JspritWrapper createAlgorithm(){
        this.algorithm = Jsprit.createAlgorithm(this.problem);
        return this;
    }

    public JspritWrapper searchSolutions(){
        this.solutions = this.algorithm.searchSolutions();
        return this;
    }

    public Collection<VehicleRoutingProblemSolution> getSolutions(){
        return this.solutions;
    }

    public VehicleRoutingAlgorithm getAlgorithm(){
        return this.algorithm;
    }

    public VehicleRoutingProblem getProblem() {
        return this.problem;
    }

    public VehicleRoutingProblemSolution bestOfSolutions(){
        return Solutions.bestOf(this.solutions);
    }

}
