package ca.bc.gov.app.endpoints;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import ca.bc.gov.app.extensions.AbstractTestContainerIntegrationTest;
import ca.bc.gov.app.extensions.WiremockLogNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@Slf4j
@DisplayName("Integrated Test | Forest Client Doing Business Controller")
class ForestClientDoingBusinessHandlerIntegrationTest extends
    AbstractTestContainerIntegrationTest {

  @Autowired
  protected WebTestClient client;

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension
      .newInstance()
      .options(
          wireMockConfig()
              .port(10000)
              .notifier(new WiremockLogNotifier())
              .asynchronousResponseEnabled(true)
              .stubRequestLoggingDisabled(false)
      )
      .configureStaticDsl(true)
      .build();

  @BeforeAll
  public static void setUp(){
    wireMockExtension.resetAll();
  }

  @Test
  @DisplayName("Those clients do business as")
  void shouldListBandValidation() {
    wireMockExtension
        .stubFor(
            get(urlMatching("/api/orgbook/name/.*"))
                .willReturn(okJson("""                    
                    {
                      "total": 1,
                      "page_size": 10,
                      "page": 1,
                      "results": [
                        {
                          "id": 789282,
                          "source_id": "BC0772006",
                          "type": "registration.registries.ca",
                          "names": [
                            {
                              "id": 1187440,
                              "text": "U3 POWER CORP.",               \s
                              "type": "entity_name",
                              "credential_id": 1218748
                            },
                            {
                              "id": 3355176,
                              "text": "837723121",               \s
                              "type": "business_number",
                              "credential_id": 3471039
                            }
                          ],
                          "addresses": [],
                          "attributes": [
                            {
                              "id": 8336043,
                              "type": "registration_date",
                              "format": "datetime",
                              "value": "2006-10-17T23:58:42+00:00",
                              "credential_id": 1218748,
                              "credential_type_id": 1
                            },
                            {
                              "id": 8336044,
                              "type": "entity_name_effective",
                              "format": "datetime",
                              "value": "2006-10-17T23:58:42+00:00",
                              "credential_id": 1218748,
                              "credential_type_id": 1
                            },
                            {
                              "id": 8336045,
                              "type": "entity_status",
                              "format": "category",
                              "value": "ACT",
                              "credential_id": 1218748,
                              "credential_type_id": 1
                            },
                            {
                              "id": 8336046,
                              "type": "entity_status_effective",
                              "format": "datetime",
                              "value": "2006-10-17T23:58:42+00:00",
                              "credential_id": 1218748,
                              "credential_type_id": 1
                            },
                            {
                              "id": 8336047,
                              "type": "entity_type",
                              "format": "category",
                              "value": "BC",
                              "credential_id": 1218748,
                              "credential_type_id": 1
                            },
                            {
                              "id": 8336048,
                              "type": "home_jurisdiction",
                              "format": "jurisdiction",
                              "value": "BC",
                              "credential_id": 1218748,
                              "credential_type_id": 1
                            },
                            {
                              "id": 8336049,
                              "type": "reason_description",
                              "format": "category",
                              "value": "Filing:ICORP",
                              "credential_id": 1218748,
                              "credential_type_id": 1
                            }
                          ],
                          "credential_set": {
                            "id": 789295,
                            "create_timestamp": "2020-02-15T20:05:45.979364Z",
                            "update_timestamp": "2020-02-15T20:05:45.979402Z",
                            "latest_credential_id": 1218748,
                            "topic_id": 789282,
                            "first_effective_date": "2006-10-17T23:58:42Z",
                            "last_effective_date": null
                          }
                        }
                      ]
                    }""")
                )
        );


    client
        .get()
        .uri("/api/client/business")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.length()").isNumber()
        .jsonPath("$.length()").isEqualTo(3)
        .consumeWith(System.out::println);


  }
}
