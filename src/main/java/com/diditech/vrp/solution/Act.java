package com.diditech.vrp.solution;

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

    private String jobId;

    private double arrTime;

    private double endTime;

}
