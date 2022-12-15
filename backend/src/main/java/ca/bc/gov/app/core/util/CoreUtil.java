package ca.bc.gov.app.core.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CoreUtil {

  public <T> T jsonStringToObj(String jsonInString, Class<T> valueType) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      return mapper.readValue(jsonInString, valueType);
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to convert JSON string: " + jsonInString + " to object of type: " +
              valueType.getCanonicalName(), e);
    }
  }


  public String objToJsonString(Object obj) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert object: " + obj + " to JSON string", e);
    }
  }


  public boolean isNullOrBlank(String str) {
    return str == null || str.trim().length() == 0;
  }

  public Date toDate(String dateStr, String format) {
    if (isNullOrBlank(dateStr)) {
      return null;
    }
    DateFormat dateFormat = new SimpleDateFormat(format);
    try {
      return dateFormat.parse(dateStr);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
          "Failed to produce Date instance out of (supposedly) " + format + " format: " + dateStr);
    }
  }

  public boolean isSame(String value1, String value2) {
    return
        StringUtils.isNotBlank(value1) &&
            StringUtils.isNotBlank(value2) &&
            value1.equalsIgnoreCase(value2);
  }

}
