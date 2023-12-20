package ca.bc.gov.app.mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Named;

public interface AbstractForestClientMapper<D, E> {

  D toDto(E entity);

  @InheritInverseConfiguration
  E toEntity(D dto);


  @Named("EmptySpaceQualifier")
  default String defaultEmptySpace(Object origin) {
    return " ";
  }

  @Named("CurrentDateTimeQualifier")
  default LocalDateTime currentDateTime(Object origin){
    return LocalDateTime.now();
  }

  @Named("InitialRevisionQualifier")
  default Long initialRevision(Object value) {
    return Objects.isNull(value) || !(value instanceof Long) ? 1L : (Long) value;
  }

}
