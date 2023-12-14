package ca.bc.gov.app.mappers;

import ca.bc.gov.app.dto.ForestClientLocationDto;
import ca.bc.gov.app.entity.ForestClientLocationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ForestClientLocationMapper extends
    AbstractForestClientMapper<ForestClientLocationDto, ForestClientLocationEntity> {

  @Override
  @Mapping(
      source = "updatedByUnit",
      target = "orgUnit",
      qualifiedByName = "InitialRevisionQualifier"
  )
  ForestClientLocationDto toDto(ForestClientLocationEntity entity);

  @Override
  @Mapping(
      target = "createdAt",
      source = "clientNumber",
      qualifiedByName = "CurrentDateTimeQualifier"
  )
  @Mapping(
      target = "updatedAt",
      source = "clientNumber",
      qualifiedByName = "CurrentDateTimeQualifier"
  )
  @Mapping(
      target = "createdByUnit",
      source = "orgUnit",
      qualifiedByName = "InitialRevisionQualifier"
  )
  @Mapping(
      target = "updatedByUnit",
      source = "orgUnit",
      qualifiedByName = "InitialRevisionQualifier"
  )
  @Mapping(
      target = "revision",
      source = "clientNumber",
      qualifiedByName = "InitialRevisionQualifier"
  )
  ForestClientLocationEntity toEntity(ForestClientLocationDto dto);
}
