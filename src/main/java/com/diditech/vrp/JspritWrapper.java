package com.diditech.vrp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.diditech.vrp.enums.TacticsEnum;
import com.diditech.vrp.job.ShipmentJob;
import com.diditech.vrp.solution.Problem;
import com.diditech.vrp.solution.costsMatrix.BaiduVehicleRoutingTransportCostsMatrix;
import com.diditech.vrp.utils.NewSolutionPrinter;
import com.diditech.vrp.utils.Point;
import com.diditech.vrp.utils.VrpResultReader;
import com.diditech.vrp.utils.VrpResultWriter;
import com.diditech.vrp.vehicle.BasicVehicle;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;

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

    protected Map<String, VehicleImpl> vehicleMap = new HashMap<>();

    protected List<ShipmentJob> jobList = new LinkedList<>();

    /**
     * 默认车队规模为有限
     */
    protected final VehicleRoutingProblem.FleetSize DEFAULT_FLEET_SIZE =
            VehicleRoutingProblem.FleetSize.FINITE;

    protected VehicleRoutingProblem.FleetSize fleetSize;

    protected VehicleRoutingTransportCosts routingCost;

    protected Set<String> releasedJobIds = new HashSet<>();

    protected Set<String> releasedVehicleIds = new HashSet<>();

    protected VrpResultReader reader = null;

    /**
     * 途经点
     */
    protected Map<String, List<Point>> wayPointsMap = new HashMap<>();

    protected Map<String, String> middleMap = new HashMap<>();

    public static JspritWrapper create() {
        return new JspritWrapper();
    }

    public static JspritWrapper create(List<String> releasedJobIds,
                                       List<String> releasedVehicleIds) {
        JspritWrapper wrapper = create();
        wrapper.addReleaseVehicles(releasedVehicleIds);
        wrapper.addReleaseJobs(releasedJobIds);
        return wrapper;
    }

    public JspritWrapper setFleetSize(VehicleRoutingProblem.FleetSize fleetSize) {
        this.fleetSize = fleetSize;
        return this;
    }

    private JspritWrapper addReleaseVehicles(List<String> vehicleIds) {
        if(CollectionUtil.isNotEmpty(vehicleIds)) {
            releasedVehicleIds.addAll(vehicleIds);
        }
        return this;
    }

    private JspritWrapper addReleaseJobs(List<String> jobIds) {
        if(CollectionUtil.isNotEmpty(jobIds)){
            releasedJobIds.addAll(jobIds);
        }
        return this;
    }

    private void addInitialWayPointsMap(Map<String, List<Point>> lastWayPointsMap,
                                  Map<String, Shipment> shipmentMap){
        List<Point> points;
        for (String jobId : shipmentMap.keySet()) {
            Shipment shipment = shipmentMap.get(jobId);
            if(shipmentMap.containsKey(jobId)){
                addWayPoints(shipment, lastWayPointsMap.get(jobId));
            }
        }
    }

    private void addWayPoints(Shipment shipment, List<Point> wayPoints){
        if(CollectionUtil.isEmpty(wayPoints)){
            return;
        }
        if(wayPointsMap.containsKey(shipment.getId())){
            throw new IllegalArgumentException("repeated way points");
        }
        wayPointsMap.put(shipment.getId(), wayPoints);
        // 通过jobId获取location，维护location和jobId的关系
        middleMap.put(shipment.getId(), shipment.getDeliveryLocation().getId());
    }

    public JspritWrapper addInitialVehicleRoutes(Problem lastProblem) {
        if (ObjectUtil.isNotNull(lastProblem)) {
            // 加载之前的单记录
            reader = new VrpResultReader(this.builder, lastProblem)
                            .addReleasedVehicleIds(releasedVehicleIds)
                            .addReleasedJobIds(releasedJobIds)
                    .read();
            if(null != reader.getRoutes()){
                this.builder.addInitialVehicleRoutes(reader.getRoutes());
            }
            addInitialWayPointsMap(lastProblem.getWayPointsMap(), reader.getShipmentMap());
        }
        return this;
    }

    /**
     * 初始单添加到车辆对象的属性中，加车时自动读取
     * @param initJobMap
     * @return
     */
    @Deprecated
    public JspritWrapper addInitialShipments(Map<String, ShipmentJob> initJobMap){
        if(CollectionUtil.isNotEmpty(initJobMap)){
            for (String vehicleId : initJobMap.keySet()) {
                addInitialShipment(vehicleId, initJobMap.get(vehicleId));
            }
        }
        return this;
    }

    /**
     * 空车初始单，需要手动创建2个vehicleRoute（pickup、delivery），并分配时间
     * @author hefan
     * @date 2021/7/30 13:57
     */
    public JspritWrapper addInitialShipment(String vehicleId, ShipmentJob job){
        // 从车的起始点-job起始点-job结束点
        VehicleImpl vehicle = vehicleMap.get(vehicleId);
        if(ObjectUtil.isNull(vehicle)){
            throw new RuntimeException("not find vehicle");
        }
        // 构造vehicleRoute
        long pickupStart = job.getStartDate().getTime();
        long pickupEnd = DateUtil.offsetMinute(job.getStartDate(),
                JspritConfig.getInstance().getPickupMaxWaitMinutes()).getTime();
        long deliveryStart = DateUtil.offsetMinute(job.getEndDate(),
                JspritConfig.getInstance().getDeliveryMinWaitMinutes()).getTime();
        long deliveryEnd = DateUtil.offsetMinute(job.getEndDate(),
                JspritConfig.getInstance().getDeliveryMaxWaitMinutes()).getTime();
        VehicleRoute.Builder vrBuilder = VehicleRoute.Builder.newInstance(vehicle);
        Shipment.Builder builder = Shipment.Builder.newInstance(job.getId())
                .setPickupLocation(job.getPickupPoint().loc())
                .setDeliveryLocation(job.getDeliveryPoint().loc())
                .addPickupTimeWindow(pickupStart, pickupEnd)
                .addDeliveryTimeWindow(deliveryStart, deliveryEnd)
                .addSizeDimension(0, job.getSizeDimension())
                .addAllRequiredSkills(job.getSkills());
        Shipment shipment = builder.build();
        vrBuilder.addPickup(shipment, new TimeWindow(pickupStart, pickupEnd));
        vrBuilder.addDelivery(shipment, new TimeWindow(deliveryStart, deliveryEnd));
        this.builder.addInitialVehicleRoute(vrBuilder.build());
        return this;
    }

    public JspritWrapper addVehicles(List<BasicVehicle> vehicles) {
        if (CollectionUtil.isNotEmpty(vehicles)) {
            vehicles.stream().forEach(vehicle -> {
                addVehicle(vehicle);
                addInitialShipment(vehicle.getId(), vehicle.getInitJob());
            });
        }
        return this;
    }

    public JspritWrapper addVehicle(BasicVehicle vehicle) {
        if (ObjectUtil.isNotNull(vehicle)) {
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
            Shipment shipment = job.build();
            addWayPoints(shipment, job.getWayPoints());
            this.jobList.add(job);
            this.builder.addJob(shipment);
        }
        return this;
    }

    private Map<String, List<Point>> convert2LocationWayPointMap(){
        Map<String, List<Point>> map = new HashMap<>();
        for (String jobId : this.wayPointsMap.keySet()) {
            map.put(this.middleMap.get(jobId), this.wayPointsMap.get(jobId));
        }
        return map;
    }

    public JspritWrapper setRoutingCost(VehicleRoutingTransportCosts routingCost) {
        this.routingCost = routingCost;
        return this;
    }

    public JspritWrapper buildProblem() {
        if(null == fleetSize){
            fleetSize = DEFAULT_FLEET_SIZE;
        }
        this.builder.setFleetSize(fleetSize);
        if(null == routingCost){
            routingCost = new BaiduVehicleRoutingTransportCostsMatrix(getLocationMap(),
                    false, JspritConfig.getInstance().getTactics(), convert2LocationWayPointMap());
        }
        this.builder.setRoutingCost(routingCost);
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

    public Problem fastBuildProblem(){
        return fastBuildProblem(true);
    }

    public Problem fastBuildProblem(boolean onlyBestSolution) {
        return this.buildProblem().createAlgorithm()
                .searchSolutions().getProblem(onlyBestSolution);
    }

    public Problem getProblem(boolean onlyBestSolution) {
        return new VrpResultWriter().write(this.problem, this.solutions,
                this.wayPointsMap, onlyBestSolution);
    }

    public Map<String, Coordinate> getLocationMap() {
        return this.builder.getLocationMap();
    }

    public VehicleRoutingProblemSolution bestOfSolutions() {
        return Solutions.bestOf(this.solutions);
    }

    public void printBestSolution() {
        NewSolutionPrinter.print(this.problem, bestOfSolutions(), NewSolutionPrinter.Print.VERBOSE);
    }

}
