package com.diditech.vrp.solution.route;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Act {
    private String type;

    private String shipmentId;

    private long arrTime;

    private long endTime;
}