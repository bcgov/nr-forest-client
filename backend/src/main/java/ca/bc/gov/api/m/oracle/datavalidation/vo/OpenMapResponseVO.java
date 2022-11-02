package ca.bc.gov.api.m.oracle.datavalidation.vo;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenMapResponseVO {

	@JsonProperty("features")
	public List<FeatureVO> features;

	public static class FeatureVO implements Serializable {

		private static final long serialVersionUID = -4888376879302258602L;
		public PropertyVO properties;
	}

	public static class PropertyVO implements Serializable {

		private static final long serialVersionUID = 3233625715673021637L;
		public String FIRST_NATION_FEDERAL_NAME;
		public String FIRST_NATION_FEDERAL_ID;
		public String ADDRESS_LINE1;
		public String ADDRESS_LINE2;
		public String OFFICE_CITY;
		public String OFFICE_PROVINCE;
		public String OFFICE_POSTAL_CODE;

	}
}
