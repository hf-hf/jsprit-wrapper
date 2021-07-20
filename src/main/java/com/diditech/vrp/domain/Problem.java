package com.diditech.vrp.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Problem {

    private ProblemType problemType;
    private VehiclesBean vehicles;
    private VehicleTypes vehicleTypes;
    private Shipments shipments;
    private SolutionsBean solutions;

}
