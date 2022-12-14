package ca.bc.gov.app.m.ob.vo;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrgBookResponseVO(
    @JsonProperty("results")
    List<ResultVO> results
){}