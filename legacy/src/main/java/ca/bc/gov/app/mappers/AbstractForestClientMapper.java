package ca.bc.gov.app.mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Named;

public interface AbstractForestClientMapper<D, E> {

  D toDto(E entity);

  @InheritInverseConfiguration
  E toEntity(D dto);

  @Named("UserIdSizeQualifier")
  default String limitUserId(String origin) {
    return origin.length() > 30 ? origin.substring(0, 30) : origin;
  }

  @Named("EmptySpaceQualifier")
  default String defaultEmptySpace(String origin) {
    return StringUtils.isBlank(origin) ? " " : origin;
  }

  @Named("AlwaysEmptySpaceQualifier")
  default String defaultEmptySpaceAlways(Object origin) {
    return " ";
  }

  @Named("CurrentDateTimeQualifier")
  default LocalDateTime currentDateTime(Object origin) {
    return LocalDateTime.now();
  }

  @Named("InitialRevisionQualifier")
  default Long initialRevision(Object value) {
    return Objects.isNull(value) || !(value instanceof Long) ? 1L : (Long) value;
  }

  @Named("LocalDateTimeDateQualifier")
  default LocalDate toLocalDate(LocalDateTime date) {
    return date == null ? null : date.toLocalDate();
  }

  @Named("LocalDateDateTimeQualifier")
  default LocalDateTime toLocalDateTime(LocalDate date) {
    return date == null ? null : date.atStartOfDay();
  }

  @Named("AddToListQualifier")
  default List<String> addToList(String value) {
    return value != null ? List.of(value) : List.of();
  }

}
