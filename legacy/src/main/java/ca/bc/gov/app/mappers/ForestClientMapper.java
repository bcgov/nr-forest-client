package ca.bc.gov.app.mappers;

import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.entity.ForestClientEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ForestClientMapper extends
    AbstractForestClientMapper<ForestClientDto, ForestClientEntity> {

  @Override
  @Mapping(
      target = "wcbFirmNumber",
      source = "clientNumber",
      qualifiedByName = "EmptySpaceQualifier",
      defaultValue = " "
  )
  @Mapping(
      target = "updatedAt",
      source = "clientNumber",
      qualifiedByName = "CurrentDateTimeQualifier"
  )
  @Mapping(
      target = "createdAt",
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
  @Mapping(
      target = "birthdate",
      source = "birthdate",
      qualifiedByName = "LocalDateDateTimeQualifier"
  )
  ForestClientEntity toEntity(ForestClientDto dto);

  @Override
  @Mapping(
      source = "updatedByUnit",
      target= "orgUnit",
      qualifiedByName = "InitialRevisionQualifier"
  )
  @Mapping(
      target = "birthdate",
      source = "birthdate",
      qualifiedByName = "LocalDateTimeDateQualifier"
  )
  ForestClientDto toDto(ForestClientEntity entity);
}
