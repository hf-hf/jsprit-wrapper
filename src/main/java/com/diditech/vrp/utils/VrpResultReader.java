package com.diditech.vrp.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.diditech.vrp.solution.InitialRoutesBean;
import com.diditech.vrp.solution.Problem;
import com.diditech.vrp.solution.ProblemType;
import com.diditech.vrp.solution.Shipments;
import com.diditech.vrp.solution.SolutionsBean;
import com.diditech.vrp.solution.VehicleTypes;
import com.diditech.vrp.solution.VehiclesBean;
import com.diditech.vrp.solution.route.Act;
import com.diditech.vrp.solution.route.Route;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * VRP结果读取
 * @author hefan
 * @date 2021/7/20 10:38
 */
@Data
public class VrpResultReader {

    private VehicleRoutingProblem.Builder vrpBuilder;

    private Problem problem;

    private Map<String, Shipment> shipmentMap = new HashMap<>();

    private Map<String, VehicleImpl> vehicleMap = new HashMap<>();

    private List<VehicleRoute> routes;

    private Collection<VehicleRoutingProblemSolution> solutionList = new ArrayList<>();

    //private Set<String> freezedJobIds = new HashSet<String>();

    private Set<String> releasedJobIds = new HashSet<>();

    private Set<String> releasedVehicleIds = new HashSet<>();

    private Map<String, List<Point>> wayPointsMap = new HashMap<>();

    public VrpResultReader(VehicleRoutingProblem.Builder vrpBuilder, Problem problem) {
        this.vrpBuilder = vrpBuilder;
        this.problem = problem;
    }

    public VrpResultReader addReleasedJobIds(Collection<String> ids){
        if(CollectionUtil.isNotEmpty(ids)){
            this.releasedJobIds.addAll(ids);
        }
        return this;
    }

    public VrpResultReader addReleasedVehicleIds(Collection<String> ids){
        if(CollectionUtil.isNotEmpty(ids)){
            this.releasedVehicleIds.addAll(ids);
        }
        return this;
    }

    public VrpResultReader read() {
        readProblemType();
        readVehiclesAndTheirTypes();

        readShipments();
        //readServices();

        //readInitialRoutes();
        readSolutions();

        addJobsAndTheirLocationsToVrp();
        return this;
    }

    public boolean hasRoutes(){
        return CollectionUtil.isNotEmpty(this.routes);
    }

    private void readProblemType() {
        ProblemType problemType = problem.getProblemType();
        String fleetSize = problemType.getFleetSize();
        if (fleetSize == null)
            vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE);
        else if (fleetSize.toUpperCase().equals(VehicleRoutingProblem.FleetSize.INFINITE.toString()))
            vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE);
        else vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
    }

    private void readShipments() {
        Shipments shipments = problem.getShipments();
        for (Shipments.Shipment shipment : shipments.getShipment()) {
            String id = shipment.getId();
            if (id == null) throw new IllegalArgumentException("shipment[@id] is missing.");
            if(releasedJobIds.contains(id)){
                continue;
            }
            Shipments.Shipment.CapacitydimensionsBean capacityDimensions = shipment.getCapacitydimensions();
            if (null == capacityDimensions || null == capacityDimensions.getDimension()) {
                throw new IllegalArgumentException("capacity of shipment is not set.");
            }

            Shipments.Shipment.CapacitydimensionsBean.DimensionBean dimensionBean = capacityDimensions.getDimension();
            Integer index = dimensionBean.getIndex();
            Integer value = dimensionBean.getContent();
            Shipment.Builder builder = Shipment.Builder.newInstance(id)
                    .addSizeDimension(index, value);

            //name = id
//            String name = shipment.getId("name");
//            if (name != null) builder.setName(name);

            //pickup location
            //pickup-locationId
            Shipments.Shipment.PickupBean pickupBean = shipment.getPickup();
            Location.Builder pickupLocationBuilder = Location.Builder.newInstance();
            String pickupLocationId = pickupBean.getLocation().getId();
            if (pickupLocationId != null) {
                pickupLocationBuilder.setId(pickupLocationId);
            }

            //pickup-coord
            com.diditech.vrp.solution.Location pickupLocation = pickupBean.getLocation();
            if (pickupLocation != null) {
                pickupLocationBuilder.setCoordinate(pickupLocation.getCoordinate());
            }

            //pickup.location.index
            Integer pickupLocationIndex = pickupLocation.getIndex();
            if (pickupLocationIndex != null) pickupLocationBuilder.setIndex(pickupLocationIndex);
            builder.setPickupLocation(pickupLocationBuilder.build());

            //pickup-serviceTime
            Double pickupServiceTime = pickupBean.getDuration();
            if (pickupServiceTime != null) builder.setPickupServiceTime(pickupServiceTime);

            //pickup-tw
            Shipments.Shipment.PickupBean.TimeWindowsBean.TimeWindowBean pickupTW
                    = pickupBean.getTimeWindows().getTimeWindow();
            if (null != pickupTW) {
                builder.addPickupTimeWindow(TimeWindow.newInstance(pickupTW.getStart(), pickupTW.getEnd()));
            }

            //delivery location
            //delivery-locationId
            Shipments.Shipment.DeliveryBean deliveryBean = shipment.getDelivery();
            Location.Builder deliveryLocationBuilder = Location.Builder.newInstance();
            String deliveryLocationId = deliveryBean.getLocation().getId();
            if (deliveryLocationId != null) {
                deliveryLocationBuilder.setId(deliveryLocationId);
//				builder.setDeliveryLocationId(deliveryLocationId);
            }

            //delivery-coord
            com.diditech.vrp.solution.Location deliveryLocation = deliveryBean.getLocation();
            if (deliveryLocation != null) {
                deliveryLocationBuilder.setCoordinate(deliveryLocation.getCoordinate());
            }

            Integer deliveryLocationIndex = deliveryLocation.getIndex();
            if (deliveryLocationIndex != null)
                deliveryLocationBuilder.setIndex(deliveryLocationIndex);
            builder.setDeliveryLocation(deliveryLocationBuilder.build());

            //delivery-serviceTime
            Double deliveryServiceTime = deliveryBean.getDuration();
            if (deliveryServiceTime != null) builder.setDeliveryServiceTime(deliveryServiceTime);

            //delivery-tw
            Shipments.Shipment.DeliveryBean.TimeWindowsBean.TimeWindowBean deliveryTW
                    = deliveryBean.getTimeWindows().getTimeWindow();
            if (null != deliveryTW) {
                builder.addDeliveryTimeWindow(TimeWindow.newInstance(deliveryTW.getStart(), deliveryTW.getEnd()));
            }

            //read skills
//            String skillString = shipmentConfig.getString("requiredSkills");
//            if (skillString != null) {
//                String cleaned = skillString.replaceAll("\\s", "");
//                String[] skillTokens = cleaned.split("[,;]");
//                for (String skill : skillTokens) builder.addRequiredSkill(skill.toLowerCase());
//            }

            //build shipment
            //Shipment shipmentResult = builder.build();
//			vrpBuilder.addJob(shipment);
            shipmentMap.put(id, builder.build());
        }
    }

    private void readSolutions() {
        SolutionsBean solutions = problem.getSolutions();
        if (solutions == null) return;
        for (SolutionsBean.SolutionBean solutionConfig : solutions.getSolution()) {
            Double totalCost = solutionConfig.getCost();
            double cost = -1;
            if (totalCost != null) cost = totalCost;
            SolutionsBean.SolutionBean.RoutesBean routeConfigs = solutionConfig.getRoutes();
            routes = new ArrayList<VehicleRoute>();
            for (Route routeConfig : routeConfigs.getRoute()) {
                //! here, driverId is set to noDriver, no matter whats in driverId.
                Driver driver = DriverImpl.noDriver();
                String vehicleId = routeConfig.getVehicleId();
                if(releasedVehicleIds.contains(vehicleId)){
                    continue;
                }
                Vehicle vehicle = getVehicle(vehicleId);
                if (vehicle == null) throw new IllegalArgumentException("vehicle is missing.");
                Long start = routeConfig.getStart();
                if (start == null) throw new IllegalArgumentException("route start-time is missing.");
                double departureTime = start;

                Long end = routeConfig.getEnd();
                if (end == null) throw new IllegalArgumentException("route end-time is missing.");

                VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(vehicle, driver);
                routeBuilder.setDepartureTime(departureTime);
                List<Act> actConfigs = routeConfig.getAct();
                for (Act actConfig : actConfigs) {
                    String type = actConfig.getType();
                    if (type == null) throw new IllegalArgumentException("act[@type] is missing.");
                    double arrTime = 0.;
                    double endTime = 0.;
                    Long arrTimeS = actConfig.getArrTime();
                    if (arrTimeS != null) arrTime = arrTimeS;
                    Long endTimeS = actConfig.getEndTime();
                    if (endTimeS != null) endTime = endTimeS;
//                    if(type.equals("break")) {
//                        Break currentbreak = getBreak(vehicleId);
//                        routeBuilder.addBreak(currentbreak);
//                    }
//                    else {
//                        String serviceId = actConfig.getString("serviceId");
//                        if (serviceId != null) {
//                            Service service = getService(serviceId);
//                            routeBuilder.addService(service);
//                        } else {
                            String shipmentId = actConfig.getShipmentId();
                            if(releasedJobIds.contains(shipmentId)){
                                continue;
                            }
                            if (shipmentId == null)
                                throw new IllegalArgumentException("either serviceId or shipmentId is missing");
                            Shipment shipment = getShipment(shipmentId);
                            if (shipment == null)
                                throw new IllegalArgumentException("shipment with id " + shipmentId + " does not exist.");
                            if (type.equals("pickupShipment")) {
                                routeBuilder.addPickup(shipment);
                            } else if (type.equals("deliverShipment")) {
                                routeBuilder.addDelivery(shipment);
                            } else
                                throw new IllegalArgumentException("type " + type + " is not supported. Use 'pickupShipment' or 'deliverShipment' here");
                        //}
                    //}
                }
                routes.add(routeBuilder.build());
            }
            VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(routes, cost);
            SolutionsBean.SolutionBean.UnassignedJobsBean unassignedJobConfigs = solutionConfig.getUnassignedJobs();
            if(null != unassignedJobConfigs){
                for (SolutionsBean.SolutionBean.UnassignedJobsBean.JobBean unassignedJobConfig : unassignedJobConfigs.getJob()) {
                    String jobId = unassignedJobConfig.getId();
                    Job job = getShipment(jobId);
                    //if (job == null) job = getService(jobId);
                    if (job == null) throw new IllegalArgumentException("cannot find unassignedJob with id " + jobId);
                    solution.getUnassignedJobs().add(job);
                }
            }

            solutionList.add(solution);
        }
    }

    private void readVehiclesAndTheirTypes() {
        //read vehicle-types
        Map<String, VehicleType> types = new HashMap<String, VehicleType>();
        VehicleTypes vehicleTypes = problem.getVehicleTypes();
        for (VehicleTypes.TypeBean typeConfig : vehicleTypes.getType()) {
            String typeId = typeConfig.getId();
            if (typeId == null) throw new IllegalArgumentException("typeId is missing.");

            VehicleTypes.TypeBean.CapacitydimensionsBean capacityDimensions = typeConfig.getCapacitydimensions();
            if (null == capacityDimensions) {
                throw new IllegalArgumentException("capacity of type is not set.");
            }

            VehicleTypeImpl.Builder typeBuilder = VehicleTypeImpl.Builder.newInstance(typeId);
            VehicleTypes.TypeBean.CapacitydimensionsBean.DimensionBean dimensionBean = capacityDimensions.getDimension();
            Integer index = dimensionBean.getIndex();
            Integer value = dimensionBean.getContent();
            typeBuilder.addCapacityDimension(index, value);

            VehicleTypes.TypeBean.CostsBean costs = typeConfig.getCosts();

            Double fix = costs.getFixed();
            Double timeC = costs.getTime();
            Double distC = costs.getDistance();

            if (fix != null) typeBuilder.setFixedCost(fix);
            if (timeC != null) typeBuilder.setCostPerTransportTime(timeC);
            if (distC != null) typeBuilder.setCostPerDistance(distC);
            VehicleType type = typeBuilder.build();
            String id = type.getTypeId();
            types.put(id, type);
        }

        //read vehicles
        VehiclesBean vehiclesBean = problem.getVehicles();

        for (VehiclesBean.Vehicle vehicleConfig : vehiclesBean.getVehicle()) {
            String vehicleId = vehicleConfig.getId();
            if (vehicleId == null) throw new IllegalArgumentException("vehicleId is missing.");
            if(releasedVehicleIds.contains(vehicleId)){
                continue;
            }
            VehicleImpl.Builder builder = VehicleImpl.Builder.newInstance(vehicleId);
            String typeId = vehicleConfig.getTypeId();
            if (typeId == null) throw new IllegalArgumentException("typeId is missing.");
            VehicleType type = types.get(typeId);
            if (type == null) throw new IllegalArgumentException("vehicleType with typeId " + typeId + " is missing.");
            builder.setType(type);

            //read startlocation
            VehiclesBean.Vehicle.StartLocation startLocation = vehicleConfig.getStartLocation();
            Location.Builder startLocationBuilder = Location.Builder.newInstance();
            String locationId = startLocation.getId();
            startLocationBuilder.setId(locationId);
            Double coordX = startLocation.getCoord().getX();
            Double coordY = startLocation.getCoord().getY();
            if (coordX == null || coordY == null) {
                throw new IllegalArgumentException("startLocation coord is null");
            } else {
                Coordinate coordinate = Coordinate.newInstance(coordX, coordY);
                startLocationBuilder.setCoordinate(coordinate);
            }
            String index = startLocation.getIndex();
            if (index != null) {
                startLocationBuilder.setIndex(Integer.parseInt(index));
            }
            builder.setStartLocation(startLocationBuilder.build());

            //read endlocation
            VehiclesBean.Vehicle.EndLocation endLocation = vehicleConfig.getEndLocation();
            Location.Builder endLocationBuilder = Location.Builder.newInstance();
            boolean hasEndLocation = false;
            String endLocationId = endLocation.getId();
            if (endLocationId != null) {
                hasEndLocation = true;
                endLocationBuilder.setId(endLocationId);
            }
            Double endCoordX = endLocation.getCoord().getX();
            Double endCoordY = endLocation.getCoord().getY();
            if (endCoordX != null && endCoordY != null) {
                Coordinate coordinate = Coordinate.newInstance(endCoordX, endCoordY);
                hasEndLocation = true;
                endLocationBuilder.setCoordinate(coordinate);
            }
            Integer endLocationIndex = endLocation.getIndex();
            if (endLocationIndex != null) {
                hasEndLocation = true;
                endLocationBuilder.setIndex(endLocationIndex);
            }
            if (hasEndLocation) builder.setEndLocation(endLocationBuilder.build());

            //read timeSchedule
            VehiclesBean.Vehicle.TimeSchedule timeSchedule = vehicleConfig.getTimeSchedule();
            Double start = timeSchedule.getStart();
            Double end = timeSchedule.getEnd();
            if (start != null) builder.setEarliestStart(start);
            if (end != null) builder.setLatestArrival(end);

            //read return2depot
            String returnToDepot = vehicleConfig.getReturnToDepot();
            if (returnToDepot != null) {
                builder.setReturnToDepot(Boolean.valueOf(returnToDepot));
            }

//            //read skills
//            String skillString = vehicleConfig.getString("skills");
//            if (skillString != null) {
//                String cleaned = skillString.replaceAll("\\s", "");
//                String[] skillTokens = cleaned.split("[,;]");
//                for (String skill : skillTokens) builder.addSkill(skill.toLowerCase());
//            }

//            // read break
//            List<HierarchicalConfiguration> breakTWConfigs = vehicleConfig.configurationsAt("breaks.timeWindows.timeWindow");
//            if (!breakTWConfigs.isEmpty()) {
//                String breakDurationString = vehicleConfig.getString("breaks.duration");
//                String id = vehicleConfig.getString("breaks.id");
//                Break.Builder current_break = Break.Builder.newInstance(id);
//                current_break.setServiceTime(Double.parseDouble(breakDurationString));
//                for (HierarchicalConfiguration twConfig : breakTWConfigs) {
//                    current_break.addTimeWindow(TimeWindow.newInstance(twConfig.getDouble("start"), twConfig.getDouble("end")));
//                }
//                builder.setBreak(current_break.build());
//            }


            //build vehicle
            VehicleImpl vehicle = builder.build();
            vrpBuilder.addVehicle(vehicle);
            vehicleMap.put(vehicleId, vehicle);
        }

    }

    private void readInitialRoutes() {
        InitialRoutesBean initialRoutesBean = problem.getInitialRoutes();
        if(null == initialRoutesBean){
            return;
        }
        for (InitialRoutesBean.RouteBean routeConfig : initialRoutesBean.getRoute()) {
            Driver driver = DriverImpl.noDriver();
            String vehicleId = routeConfig.getVehicleId();
            if(releasedVehicleIds.contains(vehicleId)){
                continue;
            }
            Vehicle vehicle = getVehicle(vehicleId);
            if (vehicle == null) throw new IllegalArgumentException("vehicle is missing.");
            Double start = routeConfig.getStart();
            if (start == null) throw new IllegalArgumentException("route start-time is missing.");
            double departureTime = start;

            VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(vehicle, driver);
            routeBuilder.setDepartureTime(departureTime);

            List<Act> actConfigs = routeConfig.getAct();
            for (Act actConfig : actConfigs) {
                String type = actConfig.getType();
                if (type == null) throw new IllegalArgumentException("act[@type] is missing.");
                Long arrTime = actConfig.getArrTime();
                Long endTime = actConfig.getEndTime();

//                String serviceId = actConfig.getString("serviceId");
//                if(type.equals("break")) {
//                    Break currentbreak = getBreak(vehicleId);
//                    routeBuilder.addBreak(currentbreak);
//                }
//                else {
//                    if (serviceId != null) {
//                        Service service = getService(serviceId);
//                        if (service == null)
//                            throw new IllegalArgumentException("service to serviceId " + serviceId + " is missing (reference in one of your initial routes). make sure you define the service you refer to here in <services> </services>.");
//                        //!!!since job is part of initial route, it does not belong to jobs in problem, i.e. variable jobs that can be assigned/scheduled
//                        freezedJobIds.add(serviceId);
//                        routeBuilder.addService(service);
//                    } else {
                        String shipmentId = actConfig.getShipmentId();
                        if (shipmentId == null)
                            throw new IllegalArgumentException("either serviceId or shipmentId is missing");
                        if(releasedJobIds.contains(shipmentId)){
                            continue;
                        }
                        Shipment shipment = getShipment(shipmentId);
                        if (shipment == null)
                            throw new IllegalArgumentException("shipment to shipmentId " + shipmentId + " is missing (reference in one of your initial routes). make sure you define the shipment you refer to here in <shipments> </shipments>.");
                        //freezedJobIds.add(shipmentId);
                        if (type.equals("pickupShipment")) {
                            routeBuilder.addPickup(shipment);
                        } else if (type.equals("deliverShipment")) {
                            routeBuilder.addDelivery(shipment);
                        } else
                            throw new IllegalArgumentException("type " + type + " is not supported. Use 'pickupShipment' or 'deliverShipment' here");
                    //}
                //}
            }
            VehicleRoute route = routeBuilder.build();
            vrpBuilder.addInitialVehicleRoute(route);
        }

    }

    private void addJobsAndTheirLocationsToVrp() {
//        for (Service service : serviceMap.values()) {
//            if (!freezedJobIds.contains(service.getId())) {
//                vrpBuilder.addJob(service);
//            }
//        }
        for (Shipment shipment : shipmentMap.values()) {
            //if (!freezedJobIds.contains(shipment.getId())) {
                vrpBuilder.addJob(shipment);
                addWayPoints(shipment.getId());
            //}
        }
    }

    private Vehicle getVehicle(String vehicleId) {
        return vehicleMap.get(vehicleId);
    }

    private Shipment getShipment(String shipmentId) {
        return shipmentMap.get(shipmentId);
    }

    private void addWayPoints(String shipmentId) {
        Map<String, List<Point>> map = this.problem.getWayPointsMap();
        if(CollectionUtil.isEmpty(map)){
            return;
        }
        List<Point> list = map.get(shipmentId);
        if(CollectionUtil.isEmpty(list)) {
            wayPointsMap.put(shipmentId, list);
        }
    }


}
