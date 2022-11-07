package ca.bc.gov.app.m.ob.vo;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrgBookResponseVO {

    @JsonProperty("results")
    public List<ResultVO> results;
    
    public static class ResultVO implements Serializable {

		private static final long serialVersionUID = 3398116613645404989L;
		
		public String value;
    }
    //TODO: Add more fields
    
}
