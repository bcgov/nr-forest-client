package ca.bc.gov.app.core.util;

import java.util.Date;

public interface CoreUtil {

	String BEAN_NAME = "coreUtil";

	Date getCurrentTime();
	
	boolean isNumber(String str);

    <T> T jsonStringToObj(String jsonInString, Class<T> valueType);

	String objToJsonString(Object obj);
	
}
