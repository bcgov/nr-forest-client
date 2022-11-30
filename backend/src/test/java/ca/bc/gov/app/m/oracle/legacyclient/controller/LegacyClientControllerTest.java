package ca.bc.gov.app.m.oracle.legacyclient.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ca.bc.gov.app.m.oracle.legacyclient.entity.ForestClientEntity;

@ExtendWith(MockitoExtension.class)
public class LegacyClientControllerTest {
	public static final String COMPANY_NAME = "test_company";
	public static final String IncorporationNumber = "BC00001";
	public static final String FirstName = "Test";
	public static final String LastName = "Green";
	public static final String BirthDate = "1980-10-20";

	@Mock
	private LegacyClientController legacyClientController;

	private ForestClientEntity client;
	private ForestClientEntity indivudual;

	@BeforeEach
	public void setup() throws Exception {

		client = new ForestClientEntity();
		client.setClientNumber("00000001");
		client.setClientName(COMPANY_NAME);
		client.setRegistryCompanyTypeCode("BC");
		client.setCorpRegnNmbr("00001");

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
		Date date = formatter.parse(BirthDate);
		indivudual = new ForestClientEntity();
		indivudual.setClientNumber("00000002");
		indivudual.setClientName(LastName);
		indivudual.setBirthdate(date);
		indivudual.setLegalFirstName(FirstName);
	}

	@Test
	public void testFindClientByIncorporationNumberOrName() {
		List<ForestClientEntity> result = Arrays.asList(client);

		// given
		given(legacyClientController.findClientByIncorporationOrName(IncorporationNumber, COMPANY_NAME)).willReturn(result);

		// when
		List<ForestClientEntity> clients = legacyClientController.findClientByIncorporationOrName(IncorporationNumber, COMPANY_NAME);

		// then
		assertThat(clients).isNotNull();
	}
	
	@Test
	public void testFindClientIndividualByNameAndDOB() {
		List<ForestClientEntity> result = Arrays.asList(indivudual);

		// given
		given(legacyClientController.findClientByNameAndBirthdate(FirstName, LastName, BirthDate)).willReturn(result);

		// when
		List<ForestClientEntity> clients = legacyClientController.findClientByNameAndBirthdate(FirstName, LastName, BirthDate);

		// then
		assertThat(clients).isNotNull();
	}
}
