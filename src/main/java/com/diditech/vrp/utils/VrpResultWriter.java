package com.diditech.vrp.utils;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                         Map<String, List<Point>> wayPointsMap,
                         boolean onlyBestSolution) {
        OutputStream xmlOutputStream = new VrpXMLWriter(problem, solutions,
                onlyBestSolution).write();
        String xmlOutput = xmlOutputStream.toString();
        JSONObject jsonObject = JSONUtil.parseFromXml(xmlOutput);
        Problem result = jsonObject.getJSONObject("problem").toBean(Problem.class);
        if(null == wayPointsMap){
            wayPointsMap = new HashMap<>(0);
        }
        result.setWayPointsMap(wayPointsMap);
        return result;
    }

}
