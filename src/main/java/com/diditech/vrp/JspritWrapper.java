package com.diditech.vrp;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.diditech.vrp.job.ShipmentJob;
import com.diditech.vrp.solution.Problem;
import com.diditech.vrp.solution.costsMatrix.BaiduVehicleRoutingTransportCostsMatrix;
import com.diditech.vrp.utils.Point;
import com.diditech.vrp.utils.VrpResultReader;
import com.diditech.vrp.utils.VrpResultWriter;
import com.diditech.vrp.vehicle.BasicVehicle;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;

/**
 * Jsprit包装器
 *
 * @author hefan
 * @date 2021/7/8 15:11
 */
public class JspritWrapper {

    protected JspritConfig config = JspritConfig.DEFAULT;

    protected VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

    protected VehicleRoutingProblem problem;

    protected VehicleRoutingAlgorithm algorithm;

    protected Collection<VehicleRoutingProblemSolution> solutions;

    protected Map<String, VehicleImpl> vehicleMap = new HashMap<>();

    protected List<ShipmentJob> jobList = new LinkedList<>();

    protected int pointNumber = 0;

    /**
     * 默认车队规模为有限
     */
    protected final VehicleRoutingProblem.FleetSize DEFAULT_FLEET_SIZE =
            VehicleRoutingProblem.FleetSize.FINITE;

    protected VehicleRoutingProblem.FleetSize fleetSize = DEFAULT_FLEET_SIZE;

    protected Set<String> frozenJobIds = new HashSet<>();

    protected Set<String> frozenVehicleIds = new HashSet<>();

    public static JspritWrapper create() {
        return new JspritWrapper();
    }

    public JspritWrapper setFleetSize(VehicleRoutingProblem.FleetSize fleetSize) {
        this.fleetSize = fleetSize;
        return this;
    }

    /**
     * 加载规划配置
     */
    public JspritWrapper setConfig(JspritConfig config) {
        this.config = config;
        return this;
    }

    /**
     * 冻结车辆
     */
    public JspritWrapper freezeVehicle(String vehicleId) {
        frozenJobIds.add(vehicleId);
        return this;
    }

    /**
     * 冻结订单
     */
    public JspritWrapper freezeJob(String jobId) {
        frozenVehicleIds.add(jobId);
        return this;
    }

    public JspritWrapper addInitialVehicleRoutes(Problem lastProblem) {
        if (ObjectUtil.isNotNull(lastProblem)) {
            // 加载之前的单记录
            VrpResultReader reader =
                    new VrpResultReader(this.builder, lastProblem).read();
            this.builder.addInitialVehicleRoutes(filterFrozen(reader.getRoutes()));
        }
        return this;
    }

    private List<VehicleRoute> filterFrozen(List<VehicleRoute> routes){
        return routes;
    }


    public JspritWrapper addVehicles(List<BasicVehicle> vehicles) {
        if (CollectionUtil.isNotEmpty(vehicles)) {
            vehicles.stream().forEach(vehicle -> addVehicle(vehicle));
        }
        return this;
    }

    public JspritWrapper addVehicle(BasicVehicle vehicle) {
        if (ObjectUtil.isNotNull(vehicle)) {
            fillPointId(vehicle.getStartPoint(), vehicle.getEndPoint());
            VehicleImpl vehicleImpl = vehicle.build();
            this.vehicleMap.put(vehicle.getId(), vehicleImpl);
            this.builder.addVehicle(vehicleImpl);
        }
        return this;
    }

    public JspritWrapper addJobs(List<ShipmentJob> jobs) {
        if (CollectionUtil.isNotEmpty(jobs)) {
            jobs.stream().forEach(job -> addJob(job));
        }
        return this;
    }


    public JspritWrapper addJob(ShipmentJob job) {
        if (ObjectUtil.isNotNull(job)) {
            fillPointId(job.getPickupPoint(), job.getDeliveryPoint());
            this.jobList.add(job);
            this.builder.addJob(job.build());
        }
        return this;
    }

    public JspritWrapper setDefaultBaiduRoutingCost() {
        Map<String, Coordinate> locationMap = getLocationMap();
        BaiduVehicleRoutingTransportCostsMatrix matrix =
                new BaiduVehicleRoutingTransportCostsMatrix(locationMap,
                        this.config, false);
        setRoutingCost(matrix);
        return this;
    }

    public JspritWrapper setRoutingCost(VehicleRoutingTransportCosts routingCost) {
        this.builder.setRoutingCost(routingCost);
        return this;
    }

    public JspritWrapper buildProblem() {
        this.builder.setFleetSize(fleetSize);
        this.problem = this.builder.build();
        return this;
    }

    public JspritWrapper createAlgorithm() {
        this.algorithm = Jsprit.createAlgorithm(this.problem);
        return this;
    }

    public JspritWrapper searchSolutions() {
        this.solutions = this.algorithm.searchSolutions();
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

    public Problem buildProblem(boolean onlyBestSolution) {
        return this.buildProblem().createAlgorithm()
                .searchSolutions().getProblem(onlyBestSolution);
    }

    public Problem getProblem(boolean onlyBestSolution) {
        return new VrpResultWriter().write(this.problem, this.solutions, onlyBestSolution);
    }

    public Map<String, Coordinate> getLocationMap() {
        return this.builder.getLocationMap();
    }

    public VehicleRoutingProblemSolution bestOfSolutions() {
        return Solutions.bestOf(this.solutions);
    }

    public void printBestSolution() {
        SolutionPrinter.print(this.problem, bestOfSolutions(), SolutionPrinter.Print.VERBOSE);
    }

    private int getAndAddNumber() {
        return pointNumber++;
    }

    /**
     * 填充点id
     *
     * @author hefan
     * @date 2021/7/20 9:30
     */
    private void fillPointId(Point... points) {
        Arrays.stream(points).forEach(p -> {
            if (ObjectUtil.isNotNull(p)) {
                p.setId(getAndAddNumber());
            }
        });
    }

}
