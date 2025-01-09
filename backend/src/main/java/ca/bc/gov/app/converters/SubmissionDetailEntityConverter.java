package ca.bc.gov.app.converters;

import ca.bc.gov.app.entity.client.SubmissionDetailEntity;
import ca.bc.gov.app.util.DatabaseCryptoUtil;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.event.AfterConvertCallback;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmissionDetailEntityConverter implements
    BeforeConvertCallback<SubmissionDetailEntity>,
    AfterConvertCallback<SubmissionDetailEntity> {

  private final DatabaseCryptoUtil cryptoUtil;

  @Override
  public Publisher<SubmissionDetailEntity> onAfterConvert(
      SubmissionDetailEntity entity,
      SqlIdentifier table
  ) {
    return cryptoUtil
        .decryptAs(entity.getOriginalOrganizationName(), String.class)
        .map(entity::withOrganizationName)
        .defaultIfEmpty(entity)
        .flatMap(updatedEntity ->
            cryptoUtil.decryptAs(updatedEntity.getOriginalClientIdentification(), String.class)
                .map(updatedEntity::withClientIdentification)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil.decryptAs(updatedEntity.getOriginalLastName(), String.class)
                .map(updatedEntity::withLastName)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil.decryptAs(updatedEntity.getOriginalMiddleName(), String.class)
                .map(updatedEntity::withMiddleName)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil.decryptAs(updatedEntity.getOriginalFirstName(), String.class)
                .map(updatedEntity::withFirstName)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil.decryptAs(updatedEntity.getOriginalBirthdate(), LocalDate.class)
                .map(updatedEntity::withBirthdate)
                .defaultIfEmpty(updatedEntity)
        );
  }

  @Override
  public Publisher<SubmissionDetailEntity> onBeforeConvert(
      SubmissionDetailEntity entity,
      SqlIdentifier table
  ) {
    return cryptoUtil
        .encryptFromString(entity.getOrganizationName())
        .map(entity::withOriginalOrganizationName)
        .defaultIfEmpty(entity)
        .flatMap(updatedEntity ->
            cryptoUtil.encryptFromString(updatedEntity.getClientIdentification())
                .map(updatedEntity::withOriginalClientIdentification)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil.encryptFromString(updatedEntity.getLastName())
                .map(updatedEntity::withOriginalLastName)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil.encryptFromString(updatedEntity.getMiddleName())
                .map(updatedEntity::withOriginalMiddleName)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil.encryptFromString(updatedEntity.getFirstName())
                .map(updatedEntity::withOriginalFirstName)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil.encryptFromLocalDate(updatedEntity.getBirthdate())
                .map(updatedEntity::withOriginalBirthdate)
                .defaultIfEmpty(updatedEntity)
        );
  }
}
