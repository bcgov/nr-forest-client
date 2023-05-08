package ca.bc.gov.app.util;

import static org.junit.jupiter.api.Assertions.*;

import ca.bc.gov.app.dto.client.ClientTypeEnum;
import ca.bc.gov.app.dto.client.LegalTypeEnum;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Unit Test | Client Validation")
class ClientValidationUtilsTest {

  @ParameterizedTest
  @MethodSource("getClientType")
  @DisplayName("getClientType and it's values")
  void shouldGetClientType(LegalTypeEnum legal, ClientTypeEnum client) {
    if(client != null){
      ClientTypeEnum clientResult = ClientValidationUtils.getClientType(legal);
      assertNotNull(clientResult);
      assertEquals(client, clientResult);
    }else{
      assertNull(ClientValidationUtils.getClientType(legal));
    }
  }

  private static Stream<Arguments> getClientType() {
    return
        Stream.of(
            Arguments.of(LegalTypeEnum.A,ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.B,ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.BC,ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.C,ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.CP,ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.EPR,ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.FOR,ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.LIC,ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.REG,ClientTypeEnum.C),
            Arguments.of(LegalTypeEnum.S,ClientTypeEnum.S),
            Arguments.of(LegalTypeEnum.XS,ClientTypeEnum.S),
            Arguments.of(LegalTypeEnum.XCP,ClientTypeEnum.A),
            Arguments.of(LegalTypeEnum.SP,ClientTypeEnum.I),
            Arguments.of(LegalTypeEnum.GP,ClientTypeEnum.P),
            Arguments.of(LegalTypeEnum.LP,ClientTypeEnum.L),
            Arguments.of(LegalTypeEnum.XL,ClientTypeEnum.L),
            Arguments.of(LegalTypeEnum.XP,ClientTypeEnum.L),
            Arguments.of(null,null)
        );
  }

}