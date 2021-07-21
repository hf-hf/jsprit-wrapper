package com.diditech.vrp;

import com.diditech.vrp.enums.TacticsEnum;

import lombok.Data;

@Data
public class JspritConfig {

    final static JspritConfig DEFAULT = new JspritConfig();

    TacticsEnum tactics = TacticsEnum.SHORT_DISTANCE_REGARDLESS_OF_ROAD_CONDITIONS;

}
