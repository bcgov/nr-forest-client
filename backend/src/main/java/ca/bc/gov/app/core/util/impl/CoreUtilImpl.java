package ca.bc.gov.app.core.util.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ca.bc.gov.app.core.CoreConstant;
import ca.bc.gov.app.core.entity.AbstractCodeDescrEntity;
import ca.bc.gov.app.core.util.CoreUtil;
import ca.bc.gov.app.core.vo.CodeDescrVO;

@Component
@Qualifier(CoreUtil.BEAN_NAME)
public class CoreUtilImpl implements CoreUtil {

    @Override
    public Date getCurrentTime() {
        return Calendar.getInstance().getTime();
    }

    @Override
    public boolean isNumber(String str) {
        return str.matches("^\\d+$");
    }
    
    @Override
    public <T> T jsonStringToObj(String jsonInString, Class<T> valueType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return mapper.readValue(jsonInString, valueType);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON string: " + jsonInString + " to object of type: " + valueType.getCanonicalName(), e);
        }
    }
    
    @Override
	public String objToJsonString(Object obj) {
		try {
	        ObjectMapper mapper = new ObjectMapper();
	        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to convert object: " + obj + " to JSON string", e);
		}
	}
    
	@Override
	public boolean isNullOrBlank(String str) {
		return str == null || str.trim().length() == 0;
	}

	@Override
	public List<String> fromCsvToStringList(String csvString) {
		if (!isNullOrBlank(csvString)) {
			return new ArrayList<>(Arrays.asList(csvString.split("\\s*,\\s*")));
		} else {
			return new ArrayList<>();
		}
	}
	
	@Override
	public String fromStringListToCsvWithAposthrophe(String csvString) {
		List<String> tempList = fromCsvToStringList(csvString);
		if (CollectionUtils.isEmpty(tempList)) {
			return null;
		}
		String finalStr = null;
		for (String listItem : tempList) {
			if (finalStr == null) {
				finalStr = CoreConstant.APOSTROPHE + listItem + CoreConstant.APOSTROPHE;
			} else {
				finalStr = finalStr + CoreConstant.CSV_SPACE_DELIMITER + CoreConstant.APOSTROPHE + listItem + CoreConstant.APOSTROPHE;
			}
		}
		return finalStr;
	}

	@Override
	public List<CodeDescrVO> toSortedCodeDescrVOs(List<? extends AbstractCodeDescrEntity> codeDescrEntities) {
		Comparator<AbstractCodeDescrEntity> comparatorByDescription = (e1, e2) ->
				(e1.getDescription()).compareTo(e2.getDescription());
		
		return codeDescrEntities.
				stream().
				sorted(comparatorByDescription).
				map(this::toCodeDescrVO).
				collect(Collectors.toList());
	}
	
	@Override
	public CodeDescrVO toCodeDescrVO(AbstractCodeDescrEntity codeDescrEntity) {
		return codeDescrEntity == null ? null : new CodeDescrVO(codeDescrEntity.getCode(), 
																codeDescrEntity.getDescription(),
																codeDescrEntity.getEffectiveDate(),
																codeDescrEntity.getExpiryDate());
	}
	
	@Override
	public Date toDate(String dateStr, String format) {
		if (isNullOrBlank(dateStr)) {
			return null;
		}
        DateFormat dateFormat = new SimpleDateFormat(format);
        try {
			return dateFormat.parse(dateStr);
		} 
        catch (ParseException e) {
			throw new IllegalArgumentException("Failed to produce Date instance out of (supposedly) " + format + " format: " + dateStr);
		}
	}
	
}
