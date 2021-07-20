package com.diditech.vrp.solution.route;

import com.diditech.vrp.enums.ActType;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行为
 * @author hefan
 * @date 2021/7/14 10:49
 */
@Data
@NoArgsConstructor
public class Act {

    private ActType type;

    private String jobId;

    private double arrTime;

    private double endTime;

}
