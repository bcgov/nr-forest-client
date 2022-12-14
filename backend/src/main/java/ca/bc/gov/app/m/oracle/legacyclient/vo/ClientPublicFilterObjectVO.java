package ca.bc.gov.app.m.oracle.legacyclient.vo;

/**
 * @author Maria Martinez, Government of BC
 * @version 1.0.0
 * @created 2022-11-03
 */

public record ClientPublicFilterObjectVO(
    String clientName,
    String clientFirstName,
    String clientMiddleName,
    String clientTypeCodesAsCsv,
    int currentPage,
    int itemsPerPage
) {
}
