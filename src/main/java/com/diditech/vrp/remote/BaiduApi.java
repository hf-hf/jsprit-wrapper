package com.diditech.vrp.remote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diditech.vrp.enums.TacticsEnum;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.util.Coordinate;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.experimental.UtilityClass;

/**
 * 百度API工具类
 *
 * @author hefan
 * @date 2021/7/14 15:40
 */
@UtilityClass
public class BaiduApi {

    private static final String AK = "gUMFqL9vB5Vf3etWx9G8nBju1yu281nL";//"3lBS83HG7YYqCf31stUsYHISNCBb2c2a";//"AsuZbkj6YlYI7tDGkomXVeMUb9ypdPdm";

    private static final String ROUTE_MATRIX_URL = "http://api.map.baidu.com/routematrix/v2/driving";

    public BaiduResponse singleRouteMatrix(Coordinate from, Coordinate to, TacticsEnum tactics) {
        String origins = from.getY() + "," + from.getX();
        String destinations = to.getY() + "," + to.getX();
        Map<String, Object> paramMap = new HashMap<>(3);
        paramMap.put("origins", origins);
        paramMap.put("destinations", destinations);
        paramMap.put("ak", AK);
        paramMap.put("tactics", tactics.getValue());
        String jsonString = HttpUtil.get(ROUTE_MATRIX_URL, paramMap);
        BaiduResponse response = JSONUtil.toBean(jsonString, BaiduResponse.class);
        return response;
    }

    public BaiduResponse routeMatrix(Map<String, Coordinate> map) {
        StringBuilder locBuilder = new StringBuilder();
        for (Coordinate coordinate : map.values()) {
            locBuilder.append(coordinate.getY() + "," + coordinate.getX())
                    .append("|");
        }
        String locations = locBuilder.substring(0, locBuilder.length() - 1);
        Map<String, Object> paramMap = new HashMap<>(3);
        paramMap.put("origins", locations);
        paramMap.put("destinations", locations);
        paramMap.put("ak", AK);
        String jsonString = HttpUtil.get(ROUTE_MATRIX_URL, paramMap);
        BaiduResponse response = JSONUtil.toBean(jsonString, BaiduResponse.class);
        return response;
    }

    public BaiduResponse routeMatrix(List<Location> list) {
        StringBuilder locBuilder = new StringBuilder();
        Coordinate coordinate;
        for (Location location : list) {
            coordinate = location.getCoordinate();
            locBuilder.append(coordinate.getY() + "," + coordinate.getX())
                    .append("|");
        }
        String locations = locBuilder.substring(0, locBuilder.length() - 1);
        Map<String, Object> paramMap = new HashMap<>(3);
        paramMap.put("origins", locations);
        paramMap.put("destinations", locations);
        paramMap.put("ak", AK);
        String jsonString = HttpUtil.get(ROUTE_MATRIX_URL, paramMap);
        BaiduResponse response = JSONUtil.toBean(jsonString, BaiduResponse.class);
        return response;
    }

    // 路线规划服务
    // http://api.map.baidu.com/direction/v2/motorcycle?origin=4846797.3,12948640.7&destination=4836829.84,12967554.88&coord_type=bd09mc&ak=您的AK //GET请求

}
