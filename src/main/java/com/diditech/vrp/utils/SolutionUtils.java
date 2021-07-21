package com.diditech.vrp.utils;

import com.diditech.vrp.solution.Problem;
import com.diditech.vrp.solution.SolutionsBean;

public class SolutionUtils {

    public static SolutionsBean.SolutionBean bestOf(Problem problem) {
        if(null == problem){
            return null;
        }
        SolutionsBean.SolutionBean best = null;
        for (SolutionsBean.SolutionBean s : problem.getSolutions().getSolution()) {
            if (best == null) best = s;
            else if (s.getCost() < best.getCost()) best = s;
        }
        return best;
    }

}