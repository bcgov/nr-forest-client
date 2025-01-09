package ca.bc.gov.app.converters;

import ca.bc.gov.app.entity.client.SubmissionContactEntity;
import ca.bc.gov.app.util.DatabaseCryptoUtil;
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
public class SubmissionContactEntityConverter implements
    BeforeConvertCallback<SubmissionContactEntity>,
    AfterConvertCallback<SubmissionContactEntity> {

  private final DatabaseCryptoUtil cryptoUtil;

  @Override
  public Publisher<SubmissionContactEntity> onAfterConvert(
      SubmissionContactEntity entity,
      SqlIdentifier table
  ) {
    return cryptoUtil
        .decryptAs(entity.getOriginalFirstName(), String.class)
        .map(entity::withFirstName)
        .defaultIfEmpty(entity)
        .flatMap(updatedEntity ->
            cryptoUtil
                .decryptAs(updatedEntity.getOriginalLastName(), String.class)
                .map(updatedEntity::withLastName)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil
                .decryptAs(updatedEntity.getOriginalBusinessPhoneNumber(), String.class)
                .map(updatedEntity::withBusinessPhoneNumber)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil
                .decryptAs(updatedEntity.getOriginalSecondaryPhoneNumber(), String.class)
                .map(updatedEntity::withSecondaryPhoneNumber)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil
                .decryptAs(updatedEntity.getOriginalFaxNumber(), String.class)
                .map(updatedEntity::withFaxNumber)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil
                .decryptAs(updatedEntity.getOriginalEmailAddress(), String.class)
                .map(updatedEntity::withEmailAddress)
                .defaultIfEmpty(updatedEntity)
        );
  }

  @Override
  public Publisher<SubmissionContactEntity> onBeforeConvert(
      SubmissionContactEntity entity,
      SqlIdentifier table
  ) {
    return cryptoUtil
        .encryptFromString(entity.getFirstName())
        .map(entity::withOriginalFirstName)
        .defaultIfEmpty(entity)
        .flatMap(updatedEntity ->
            cryptoUtil
                .encryptFromString(updatedEntity.getLastName())
                .map(updatedEntity::withOriginalLastName)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil
                .encryptFromString(updatedEntity.getBusinessPhoneNumber())
                .map(updatedEntity::withOriginalBusinessPhoneNumber)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil
                .encryptFromString(updatedEntity.getSecondaryPhoneNumber())
                .map(updatedEntity::withOriginalSecondaryPhoneNumber)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil
                .encryptFromString(updatedEntity.getFaxNumber())
                .map(updatedEntity::withOriginalFaxNumber)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil
                .encryptFromString(updatedEntity.getEmailAddress())
                .map(updatedEntity::withOriginalEmailAddress)
                .defaultIfEmpty(updatedEntity)
        );
  }
}
