package com.diditech.vrp;

import java.util.Collection;
import java.util.Date;
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
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
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

    protected VehicleRoutingProblem.FleetSize fleetSize = DEFAULT_FLEET_SIZE;

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
            this.builder.addInitialVehicleRoutes(reader.getRoutes());
            addInitialWayPointsMap(lastProblem.getWayPointsMap(), reader.getShipmentMap());
        }
        return this;
    }

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
        Date pickupStart = job.getDate();
        Date pickupEnd = DateUtil.offsetMinute(job.getDate(),
                JspritConfig.getInstance().getPickup_wait_minutes());
        Date deliveryStart = DateUtil.offsetMinute(job.getDate(),
                JspritConfig.getInstance().getDelivery_wait_start_minutes());
        Date deliveryend = DateUtil.offsetMinute(job.getDate(),
                JspritConfig.getInstance().getDelivery_wait_end_minutes());
        VehicleRoute.Builder vrBuilder = VehicleRoute.Builder.newInstance(vehicle);
        Service.Builder<Pickup> pickup = Pickup.Builder.newInstance(job.getId())
                .setLocation(job.getPickupPoint().loc())
                .setTimeWindow(new TimeWindow(pickupStart.getTime(), pickupEnd.getTime()))
                .addSizeDimension(0, job.getSizeDimension())
                .addAllRequiredSkills(job.getSkills());
        // TODO 结束时间为delivery end time
        Service.Builder<Delivery> delivery = Delivery.Builder.newInstance(job.getId())
                .setLocation(job.getPickupPoint().loc())
                .setTimeWindow(new TimeWindow(deliveryStart.getTime(), deliveryend.getTime()))
                .addSizeDimension(0, job.getSizeDimension())
                .addAllRequiredSkills(job.getSkills());
        vrBuilder.addPickup(pickup.build());
        vrBuilder.addDelivery(delivery.build());
        this.builder.addInitialVehicleRoute(vrBuilder.build());
        return this;
    }

    public JspritWrapper addVehicles(List<BasicVehicle> vehicles) {
        if (CollectionUtil.isNotEmpty(vehicles)) {
            vehicles.stream().forEach(vehicle -> addVehicle(vehicle));
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


    public JspritWrapper setDefaultBaiduRoutingCost() {
        Map<String, Coordinate> locationMap = getLocationMap();
        BaiduVehicleRoutingTransportCostsMatrix matrix =
                new BaiduVehicleRoutingTransportCostsMatrix(locationMap, false, convert2LocationWayPointMap());
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
        SolutionPrinter.print(this.problem, bestOfSolutions(), SolutionPrinter.Print.VERBOSE);
    }

}
