package ca.bc.gov.app.m.postgres.client.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import ca.bc.gov.app.core.vo.CodeDescrVO;
import ca.bc.gov.app.m.postgres.client.entity.ClientTypeCodeEntity;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClientControllerTest {
  public static final String CLIENT_NUMBER = "00000008";
  public static final String CLIENT_NUMBER_INVALID = "abc";

  @Mock
  private ClientController clientController;

  private ClientTypeCodeEntity clientTypeCode;
  private CodeDescrVO codeDescrVO;

  @BeforeEach
  public void setup() throws Exception {
    Date today = new Date();
    Date tomorrow = new Date(today.getTime() + (1000 * 60 * 60 * 24));

    clientTypeCode = new ClientTypeCodeEntity();
    clientTypeCode.setCode("ACT");
    clientTypeCode.setDescription("Active");
    clientTypeCode.setEffectiveDate(today);
    clientTypeCode.setExpiryDate(tomorrow);

    codeDescrVO = new CodeDescrVO(clientTypeCode.getCode(), clientTypeCode.getDescription(),
        clientTypeCode.getEffectiveDate(), clientTypeCode.getExpiryDate(), null);
  }

  @Test
  public void testFindActiveClientTypeCodes() {
    List<CodeDescrVO> result = Arrays.asList(codeDescrVO);

    // given
    given(clientController.findActiveClientTypeCodes())
        .willReturn(result);

    // when
    List<CodeDescrVO> activeClientTypes = clientController.findActiveClientTypeCodes();

    // then
    assertThat(activeClientTypes).isNotNull();

    // todo: how to test if only returns active ones
  }
}
