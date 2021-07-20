package com.diditech.vrp.utils;

import java.io.OutputStream;
import java.util.Collection;

import com.diditech.vrp.domain.Problem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class VrpJsonWriter {

    public Problem write(VehicleRoutingProblem problem,
                         Collection<VehicleRoutingProblemSolution> solutions,
                         boolean onlyBestSolution) {
        OutputStream xmlOutputStream = new VrpXMLWriter(problem, solutions,
                onlyBestSolution).write();
        String xmlOutput = xmlOutputStream.toString();
        JSONObject jsonObject = JSONUtil.parseFromXml(xmlOutput);
        System.out.println(jsonObject.toString());
        return jsonObject.getJSONObject("problem").toBean(Problem.class);
    }

}
