package com.diditech.vrp.vehicle;

import java.util.Date;

public interface ITimeWindow<T> {

    T setEarliestStart(Date date);

    T setLatestArrival(Date date);

}
