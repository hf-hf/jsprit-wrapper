package com.diditech.vrp.utils;

import java.util.Collection;

import com.diditech.vrp.solution.VrpSolution;

public class Solutions2 {

    public static VrpSolution bestOf(Collection<VrpSolution> solutions) {
        VrpSolution best = null;
        for (VrpSolution s : solutions) {
            if (best == null) best = s;
            else if (s.getCost() < best.getCost()) best = s;
        }
        return best;
    }

}