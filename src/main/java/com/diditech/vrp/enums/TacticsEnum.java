package com.diditech.vrp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 百度批量算路策略
 * @author hefan
 * @date 2021/7/21 9:07
 */
@Getter
@AllArgsConstructor
public enum TacticsEnum {

    /**
     *
     * 10： 不走高速；
     * 11：常规路线，即多数用户常走的一条经验路线，满足大多数场景需求，是较推荐的一个策略
     * 12： 距离较短（考虑路况）：即距离相对较短的一条路线，但并不一定是一条优质路线。计算耗时时，考虑路况对耗时的影响；
     * 13： 距离较短（不考虑路况）：路线同以上，但计算耗时时，不考虑路况对耗时的影响，可理解为在路况完全通畅时预计耗时。
     * 注：除13外，其他偏好的耗时计算都考虑实时路况
     * 默认为13：最短距离（不考虑路况）
     */
    NO_HIGH_SPEED(10),
    REGULAR_ROUTE(11),
    SHORT_DISTANCE_CONSIDERING_ROAD_CONDITIONS(12),
    SHORT_DISTANCE_REGARDLESS_OF_ROAD_CONDITIONS(13);

    private int value;

}
