package com.diditech.vrp.utils;

import java.util.Collection;

import com.diditech.vrp.solution.VRPSolution;

public class Solutions {

    public static VRPSolution bestOf(Collection<VRPSolution> solutions) {
        VRPSolution best = null;
        for (VRPSolution s : solutions) {
            if (best == null) best = s;
            else if (s.getCost() < best.getCost()) best = s;
        }
        return best;
    }

}