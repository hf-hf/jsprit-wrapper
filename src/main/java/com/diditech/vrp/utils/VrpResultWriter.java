package com.diditech.vrp.utils;

import java.io.OutputStream;
import java.util.Collection;

import com.diditech.vrp.solution.Problem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * VRP结果写入
 *
 * @author hefan
 * @date 2021/7/20 10:38
 */
public class VrpResultWriter {

    public Problem write(VehicleRoutingProblem problem,
                         Collection<VehicleRoutingProblemSolution> solutions,
                         boolean onlyBestSolution) {
        OutputStream xmlOutputStream = new VrpXMLWriter(problem, solutions,
                onlyBestSolution).write();
        String xmlOutput = xmlOutputStream.toString();
        JSONObject jsonObject = JSONUtil.parseFromXml(xmlOutput);
        return jsonObject.getJSONObject("problem").toBean(Problem.class);
    }

}
