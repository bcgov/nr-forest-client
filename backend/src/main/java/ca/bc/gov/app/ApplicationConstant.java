package ca.bc.gov.app;

import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentAccessRequestDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentAccessTypeDto;
import ca.bc.gov.app.dto.bcregistry.BcRegistryDocumentRequestBodyDto;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This class contains constants used throughout the application.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApplicationConstant {
  public static final String POSTGRES_ATTRIBUTE_SCHEMA = "nrfc";

  public static final String USERID_HEADER = "x-user-id";
  public static final String USERMAIL_HEADER = "x-user-email";
  public static final String USERNAME_HEADER = "x-user-name";
  
  public static final String INDIVIDUAL_CLIENT_TYPE_CODE = "I";
  public static final String REG_SOLE_PROPRIETORSHIP_CLIENT_TYPE_CODE = "RSP";
  public static final String UNREG_SOLE_PROPRIETORSHIP_CLIENT_TYPE_CODE = "USP";
  
  public static final BcRegistryDocumentRequestBodyDto
      BUSINESS_SUMMARY_FILING_HISTORY =
      new BcRegistryDocumentRequestBodyDto(
          new BcRegistryDocumentAccessRequestDto(
              List.of(
                  new BcRegistryDocumentAccessTypeDto("BUSINESS_SUMMARY_FILING_HISTORY")
              )
          )
      );

  public static final String SUBMISSION_DETAILS_QUERY = """
      SELECT
        s.submission_id,
        ssc.description as status,
        stc.description as submission_type,
        s.submission_date,
        s.update_timestamp,
        s.update_user,
        btc.description as business_type,
        sd.incorporation_number,
        sd.client_number,
        sd.organization_name,
        ctc.description as client_type,
        sd.good_standing_ind as good_standing,
        sd.birthdate
      FROM nrfc.submission s
      left join nrfc.submission_status_code ssc on ssc.submission_status_code = s.submission_status_code\s
      left join nrfc.submission_type_code stc on stc.submission_type_code = s.submission_type_code
      left join nrfc.submission_detail sd on sd.submission_id = s.submission_id\s
      left join nrfc.business_type_code btc on btc.business_type_code = sd.business_type_code\s
      left join nrfc.client_type_code ctc on ctc.client_type_code = sd.client_type_code\s
      where s.submission_id = :submissionId""";
  
  public static final String SUBMISSION_CONTACTS_QUERY = """
      SELECT
        ROW_NUMBER() OVER (order by sc.submission_contact_id ) AS index,
        sc.contact_type_code,
        ctc.description as contact_desc,
        sc.first_name,
        sc.last_name,
        sc.business_phone_number,
        sc.email_address,
        (select STRING_AGG(sl.location_name,', ') as locations from nrfc.submission_location sl left join nrfc.submission_location_contact_xref slcx on slcx.submission_location_id = sl.submission_location_id left join nrfc.submission_contact sc on sc.submission_contact_id = slcx.submission_contact_id where sl.submission_id = :submissionId) as locations,
        sc.idp_user_id
      FROM nrfc.submission_contact sc
      left join nrfc.contact_type_code ctc on ctc.contact_type_code = sc.contact_type_code
      where sc.submission_id = :submissionId""";
  
  public static final String SUBMISSION_LOCATION_QUERY = """
      SELECT
        ROW_NUMBER() OVER (order by sl.submission_location_id ) AS index,
        sl.street_address,
        sl.country_code,
        cc.description as country_desc,
        sl.province_code,
        pc.description as province_desc,
        sl.city_name,
        sl.postal_code,
        sl.location_name
      FROM nrfc.submission_location sl
      left join nrfc.country_code cc on cc.country_code = sl.country_code
      left join nrfc.province_code pc on (pc.province_code = sl.province_code and pc.country_code = cc.country_code)
      where sl.submission_id = :submissionId
      order by sl.submission_location_id""";
  
  public static final String SUBMISSION_TYPE = "submissionType";
  public static final String SUBMISSION_ID = "submissionId";
  public static final String REFRESH_TOKEN = "refreshToken";
  public static final String ID_TOKEN = "idToken";
  public static final String ACCESS_TOKEN = "accessToken";
}

