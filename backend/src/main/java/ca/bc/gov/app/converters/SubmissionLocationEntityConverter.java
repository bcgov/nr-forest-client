package ca.bc.gov.app.converters;

import ca.bc.gov.app.entity.client.SubmissionLocationEntity;
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
public class SubmissionLocationEntityConverter implements
    BeforeConvertCallback<SubmissionLocationEntity>,
    AfterConvertCallback<SubmissionLocationEntity>
{

  private final DatabaseCryptoUtil cryptoUtil;

  @Override
  public Publisher<SubmissionLocationEntity> onAfterConvert(
      SubmissionLocationEntity entity,
      SqlIdentifier table
  ) {
    return cryptoUtil
        .decryptAs(entity.getOriginalStreetAddress(), String.class)
        .map(entity::withStreetAddress)
        .defaultIfEmpty(entity)
        .flatMap(updatedEntity ->
            cryptoUtil.decryptAs(updatedEntity.getOriginalComplementaryAddress1(), String.class)
                .map(updatedEntity::withComplementaryAddress1)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil.decryptAs(updatedEntity.getOriginalComplementaryAddress2(), String.class)
                .map(updatedEntity::withComplementaryAddress2)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil.decryptAs(updatedEntity.getOriginalCityName(), String.class)
                .map(updatedEntity::withCityName)
                .defaultIfEmpty(updatedEntity)
        )
        .flatMap(updatedEntity ->
            cryptoUtil.decryptAs(updatedEntity.getOriginalPostalCode(), String.class)
                .map(updatedEntity::withPostalCode)
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
  public Publisher<SubmissionLocationEntity> onBeforeConvert(SubmissionLocationEntity entity,
      SqlIdentifier table) {
    return
        cryptoUtil
            .encryptFromString(entity.getStreetAddress())
            .map(entity::withOriginalStreetAddress)
            .defaultIfEmpty(entity)
            .flatMap(updatedEntity ->
                cryptoUtil.encryptFromString(updatedEntity.getComplementaryAddress1())
                    .map(updatedEntity::withOriginalComplementaryAddress1)
                    .defaultIfEmpty(updatedEntity)
            )
            .flatMap(updatedEntity ->
                cryptoUtil.encryptFromString(updatedEntity.getComplementaryAddress2())
                    .map(updatedEntity::withOriginalComplementaryAddress2)
                    .defaultIfEmpty(updatedEntity)
            )
            .flatMap(updatedEntity ->
                cryptoUtil.encryptFromString(updatedEntity.getCityName())
                    .map(updatedEntity::withOriginalCityName)
                    .defaultIfEmpty(updatedEntity)
            )
            .flatMap(updatedEntity ->
                cryptoUtil.encryptFromString(updatedEntity.getPostalCode())
                    .map(updatedEntity::withOriginalPostalCode)
                    .defaultIfEmpty(updatedEntity)
            )
            .flatMap(updatedEntity ->
                cryptoUtil.encryptFromString(updatedEntity.getBusinessPhoneNumber())
                    .map(updatedEntity::withOriginalBusinessPhoneNumber)
                    .defaultIfEmpty(updatedEntity)
            )
            .flatMap(updatedEntity ->
                cryptoUtil.encryptFromString(updatedEntity.getSecondaryPhoneNumber())
                    .map(updatedEntity::withOriginalSecondaryPhoneNumber)
                    .defaultIfEmpty(updatedEntity)
            )
            .flatMap(updatedEntity ->
                cryptoUtil.encryptFromString(updatedEntity.getFaxNumber())
                    .map(updatedEntity::withOriginalFaxNumber)
                    .defaultIfEmpty(updatedEntity)
            )
            .flatMap(updatedEntity ->
                cryptoUtil.encryptFromString(updatedEntity.getEmailAddress())
                    .map(updatedEntity::withOriginalEmailAddress)
                    .defaultIfEmpty(updatedEntity)
            );
  }
}
