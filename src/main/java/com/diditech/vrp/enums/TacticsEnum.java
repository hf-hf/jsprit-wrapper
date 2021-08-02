package com.diditech.vrp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 百度批量算路策略
 *
 * @author hefan
 * @date 2021/7/21 9:07
 */
@Getter
@AllArgsConstructor
public enum TacticsEnum {

    /**
     * 0：常规路线，即多数用户常走的一条经验路线，满足大多数场景需求，是较推荐的一个策略
     * 1：不走高速
     * 2：躲避拥堵
     * 3：距离较短
     */
    CONVENTIONAL_ROUTE(0),
    NO_HIGHWAY(1),
    AVOID_CONGESTION(2),
    SHORT_DISTANCE(3);

    private int value;

    public static TacticsEnum get(int value) {
        for (TacticsEnum tactics : TacticsEnum.values()) {
            if (tactics.getValue() == value) {
                return tactics;
            }
        }
        return null;
    }

}
