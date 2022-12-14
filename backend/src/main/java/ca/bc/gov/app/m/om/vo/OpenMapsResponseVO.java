package ca.bc.gov.app.m.om.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenMapsResponseVO(
    @JsonProperty("features")
    List<FeatureVO> features
) {
}
