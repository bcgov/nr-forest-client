package ca.bc.gov.app.dto.client;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientBusinessInformationDto(
        String registrationNumber,
        String businessName,
        String businessType,
        String clientType,
        String goodStandingInd,
        String legalType,
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate birthdate,
        String district) {

    private static final String DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";

    public static class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateString = p.getText();
            if (dateString.isEmpty()) {
                return null;
            } 
            else if (!dateString.matches(DATE_PATTERN)) {
              return null;
            } 
            else {
                try {
                    return LocalDate.parse(dateString);
                } 
                catch (DateTimeParseException e) {
                  return null;
                }
            }
        }
    }

    public Map<String, Object> description() {
        return Map.of(
                "registrationNumber", StringUtils.isBlank(registrationNumber) ? "" : registrationNumber,
                "name", StringUtils.isBlank(businessName) ? "" : businessName,
                "businessType", StringUtils.isBlank(businessType) ? "" : businessType,
                "clientType", StringUtils.isBlank(clientType) ? "" : clientType,
                "goodStanding", StringUtils.isBlank(goodStandingInd) ? "" : goodStandingInd,
                "legalType", StringUtils.isBlank(legalType) ? "" : legalType,
                "birthdate", Optional.ofNullable(birthdate).isPresent() ? birthdate : null,
                "district", StringUtils.isBlank(district) ? "" : district
        );
    }
}
