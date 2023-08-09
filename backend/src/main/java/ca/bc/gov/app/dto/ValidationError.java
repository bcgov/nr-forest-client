package ca.bc.gov.app.dto;

import java.io.Serializable;

public record ValidationError(String fieldId, String errorMsg) implements Serializable {
}
