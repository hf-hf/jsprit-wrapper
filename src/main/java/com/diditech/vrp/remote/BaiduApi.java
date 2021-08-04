package com.diditech.vrp.remote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diditech.vrp.JspritConfig;
import com.diditech.vrp.enums.TacticsEnum;
import com.diditech.vrp.utils.Point;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.util.Coordinate;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 百度API工具类
 *
 * @author hefan
 * @date 2021/7/14 15:40
 */
@Slf4j
@UtilityClass
public class BaiduApi {

    private static final String ROUTE_MATRIX_URL = "http://api.map.baidu.com/routematrix/v2/driving";

    private static final String LITE_DIRECTION_DRIVING_URL = "https://api.map.baidu.com/directionlite/v1/driving";

    public BaiduLiteDirectionResponse liteDirectionDriving(Coordinate from, Coordinate to,
                                                           TacticsEnum tactics, List<Point> wayPoints){
        String origin = from.getY() + "," + from.getX();
        String destination = to.getY() + "," + to.getX();
        Map<String, Object> paramMap = new HashMap<>(4);
        paramMap.put("origin", origin);
        paramMap.put("destination", destination);
        paramMap.put("ak", getAk());
        paramMap.put("tactics", tactics.getValue());
        if(CollectionUtil.isNotEmpty(wayPoints)){
            paramMap.put("waypoints", getMultiPointsStr(wayPoints));
        }
        String jsonString = HttpUtil.get(LITE_DIRECTION_DRIVING_URL, paramMap);
        BaiduLiteDirectionResponse response = JSONUtil.toBean(jsonString, BaiduLiteDirectionResponse.class);
        return response;
    }

    public String getMultiPointsStr(List<Point> points){
        StringBuilder pointSb = new StringBuilder();
        for (Point point : points) {
            pointSb.append(point.getLat() + "," + point.getLng())
                    .append("|");
        }
        return pointSb.substring(0, pointSb.length() - 1);
    }

    public BaiduRouteMatrixResponse singleRouteMatrix(Coordinate from, Coordinate to, TacticsEnum tactics) {
        String origins = from.getY() + "," + from.getX();
        String destinations = to.getY() + "," + to.getX();
        Map<String, Object> paramMap = new HashMap<>(3);
        paramMap.put("origins", origins);
        paramMap.put("destinations", destinations);
        paramMap.put("ak", getAk());
        paramMap.put("tactics", tactics.getValue());
        String jsonString = HttpUtil.get(ROUTE_MATRIX_URL, paramMap);
        BaiduRouteMatrixResponse response = JSONUtil.toBean(jsonString, BaiduRouteMatrixResponse.class);
        return response;
    }

    public BaiduRouteMatrixResponse routeMatrix(Map<String, Coordinate> map) {
        StringBuilder locBuilder = new StringBuilder();
        for (Coordinate coordinate : map.values()) {
            locBuilder.append(coordinate.getY() + "," + coordinate.getX())
                    .append("|");
        }
        String locations = locBuilder.substring(0, locBuilder.length() - 1);
        Map<String, Object> paramMap = new HashMap<>(3);
        paramMap.put("origins", locations);
        paramMap.put("destinations", locations);
        paramMap.put("ak",  getAk());
        String jsonString = HttpUtil.get(ROUTE_MATRIX_URL, paramMap);
        BaiduRouteMatrixResponse response = JSONUtil.toBean(jsonString, BaiduRouteMatrixResponse.class);
        return response;
    }

    public BaiduRouteMatrixResponse routeMatrix(List<Location> list) {
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
        paramMap.put("ak",  getAk());
        String jsonString = HttpUtil.get(ROUTE_MATRIX_URL, paramMap);
        BaiduRouteMatrixResponse response = JSONUtil.toBean(jsonString, BaiduRouteMatrixResponse.class);
        return response;
    }

    /**
     * 获取百度AK，如果未配置则使用默认
     * @author hefan
     * @date 2021/7/28 16:07
     */
    private String getAk() {
        return JspritConfig.getInstance().getBaiduAk();
    }

    // 路线规划服务
    // http://api.map.baidu.com/direction/v2/motorcycle?origin=4846797.3,12948640.7&destination=4836829.84,12967554.88&coord_type=bd09mc&ak=您的AK //GET请求

}
