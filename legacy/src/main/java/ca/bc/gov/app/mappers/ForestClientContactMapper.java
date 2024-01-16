package ca.bc.gov.app.mappers;

import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.entity.ForestClientContactEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ForestClientContactMapper extends
    AbstractForestClientMapper<ForestClientContactDto, ForestClientContactEntity> {

  @Override
  @Mapping(
      source = "updatedByUnit",
      target = "orgUnit",
      qualifiedByName = "InitialRevisionQualifier"
  )
  ForestClientContactDto toDto(ForestClientContactEntity entity);

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
  @Mapping(
      source = "createdBy",
      target = "createdBy",
      qualifiedByName = "UserIdSizeQualifier"
  )
  @Mapping(
      source = "updatedBy",
      target = "updatedBy",
      qualifiedByName = "UserIdSizeQualifier"
  )
  ForestClientContactEntity toEntity(ForestClientContactDto dto);
}
