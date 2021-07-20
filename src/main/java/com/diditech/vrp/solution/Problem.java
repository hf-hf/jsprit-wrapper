package com.diditech.vrp.solution;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Problem {

    private int pointNumber;
    private ProblemType problemType;
    private VehiclesBean vehicles;
    private VehicleTypes vehicleTypes;
    private Shipments shipments;
    private SolutionsBean solutions;

}
