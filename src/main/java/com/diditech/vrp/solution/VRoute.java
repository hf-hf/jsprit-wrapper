package com.diditech.vrp.solution;

import java.util.ArrayList;
import java.util.List;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车辆路线
 *
 * @author hefan
 * @date 2021/7/14 10:48
 */
@Data
@NoArgsConstructor
public class VRoute {

    private String vehicleId;

    private double start;

    private double end;

    private List<Act> actList;

    public VRoute convert(VehicleRoute route) {
        vehicleId = route.getVehicle().getId();
        start = route.getStart().getEndTime();
        end = route.getEnd().getArrTime();

        List<TourActivity> tActList = route.getTourActivities().getActivities();
        actList = new ArrayList<>(tActList.size());
        Act act;
        for (TourActivity tAct : tActList) {
            act = new Act();
            //act.setJobId(tAct.getName());
            if (tAct instanceof TourActivity.JobActivity) {
                Job job = ((TourActivity.JobActivity) tAct).getJob();
                act.setJobId(job.getId());
            }
            act.setArrTime(tAct.getArrTime());
            act.setEndTime(tAct.getEndTime());
            actList.add(act);
        }
        return this;
    }


}
