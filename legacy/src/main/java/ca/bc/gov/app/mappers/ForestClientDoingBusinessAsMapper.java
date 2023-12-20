package ca.bc.gov.app.mappers;

import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.entity.ClientDoingBusinessAsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ForestClientDoingBusinessAsMapper extends
    AbstractForestClientMapper<ClientDoingBusinessAsDto, ClientDoingBusinessAsEntity> {

  @Override
  @Mapping(
      source = "updatedByUnit",
      target = "orgUnit",
      qualifiedByName = "InitialRevisionQualifier"
  )
  ClientDoingBusinessAsDto toDto(ClientDoingBusinessAsEntity entity);

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
  ClientDoingBusinessAsEntity toEntity(ClientDoingBusinessAsDto dto);
}
