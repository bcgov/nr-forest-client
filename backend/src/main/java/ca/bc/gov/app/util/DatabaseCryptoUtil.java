package ca.bc.gov.app.util;

import io.r2dbc.postgresql.codec.Json;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DatabaseCryptoUtil {

  private final DatabaseClient databaseClient;

  /**
   * Decrypts the provided byte array into an object of the specified class type. This method uses
   * the database to perform the decryption using the `pgp_sym_decrypt` function. If the provided
   * byte array is null, it returns an empty Mono.
   *
   * @param <T>   The type of the object to be returned after decryption.
   * @param data  The byte array to be decrypted.
   * @param clazz The class type of the object to be returned after decryption.
   * @return A Mono emitting the decrypted object of the specified class type, or an empty Mono if
   * the input data is null.
   */
  public <T> Mono<T> decryptAs(byte[] data, Class<T> clazz) {
    if (Objects.isNull(data)) {
      return Mono.empty();
    }
    return databaseClient.sql(generateDecryptQuery(clazz))
        .bind("data", data)
        .map((row, rowMetadata) -> row.get("decrypted_data", clazz))
        .first();
  }

  /**
   * Encrypts the provided string into a byte array. This method uses the database to perform the
   * encryption using the `pgp_sym_encrypt` function. If the provided string is null, it returns an
   * empty Mono.
   *
   * @param data The string to be encrypted.
   * @return A Mono emitting the encrypted byte array, or an empty Mono if the input string is null.
   */
  public Mono<byte[]> encryptFromString(String data) {
    if (Objects.isNull(data)) {
      return Mono.empty();
    }
    return databaseClient.sql(
            "SELECT pgp_sym_encrypt(:data, current_setting('cryptic.key')) AS encrypted_data")
        .bind("data", data).map((row, rowMetadata) -> row.get("encrypted_data", byte[].class))
        .first();
  }

  /**
   * Encrypts the provided LocalDate into a byte array. This method converts the LocalDate to a
   * string formatted as ISO_DATE and then encrypts it using the `encryptFromString` method. If the
   * provided LocalDate is null, it returns an empty Mono.
   *
   * @param data The LocalDate to be encrypted.
   * @return A Mono emitting the encrypted byte array, or an empty Mono if the input LocalDate is
   * null.
   */
  public Mono<byte[]> encryptFromLocalDate(LocalDate data) {
    if (Objects.isNull(data)) {
      return Mono.empty();
    }
    return encryptFromString(data.format(DateTimeFormatter.ISO_DATE));
  }

  private <T> String generateDecryptQuery(Class<T> clazz){
    String decryptQuery = "SELECT pgp_sym_decrypt(:data, current_setting('cryptic.key'))%s AS decrypted_data";
    if(clazz.equals(Json.class)){
      return String.format(decryptQuery, "::jsonb");
    }
    if(clazz.equals(LocalDate.class)){
      return String.format(decryptQuery, "::date");
    }
    return String.format(decryptQuery, StringUtils.EMPTY);
  }

}