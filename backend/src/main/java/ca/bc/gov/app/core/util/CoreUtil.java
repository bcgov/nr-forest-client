package ca.bc.gov.app.core.util;

import java.util.Date;
import java.util.List;

import ca.bc.gov.app.core.entity.AbstractCodeDescrEntity;
import ca.bc.gov.app.core.vo.CodeDescrVO;

public interface CoreUtil {

	String BEAN_NAME = "coreUtil";

	Date getCurrentTime();
	
	boolean isNumber(String str);

    <T> T jsonStringToObj(String jsonInString, Class<T> valueType);

	String objToJsonString(Object obj);

	boolean isNullOrBlank(String str);

	List<String> fromCsvToStringList(String csvString);

	String fromStringListToCsvWithAposthrophe(String csvString);

	List<CodeDescrVO> toSortedCodeDescrVOs(List<? extends AbstractCodeDescrEntity> codeDescrEntities);

	CodeDescrVO toCodeDescrVO(AbstractCodeDescrEntity codeDescrEntity);

	Date toDate(String dateStr, String format);

}
