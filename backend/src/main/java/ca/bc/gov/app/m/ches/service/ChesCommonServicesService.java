package ca.bc.gov.app.m.ches.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ChesCommonServicesService {

  private URI toURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Failed to convert input value to URI. " + e.toString());
    }
  }

  private String getToken() {

    try {
      OAuthClient client = new OAuthClient(new URLConnectionClient());

      OAuthClientRequest request = OAuthClientRequest.tokenLocation(System.getenv("CHES_TOKEN_URL"))
          .setGrantType(GrantType.CLIENT_CREDENTIALS).setClientId(System.getenv("CHES_CLIENT_ID"))
          .setClientSecret(System.getenv("CHES_CLIENT_SECRET")).setScope("").buildBodyMessage();

      String token =
          client.accessToken(request, OAuth.HttpMethod.POST, OAuthJSONAccessTokenResponse.class)
              .getAccessToken();

      return token;
    } catch (Exception e) {
      log.error("Failed to get email authentication token" + e.toString());
      return "";
    }

  }

  public ResponseEntity<Object> sendEmail(String emailTo, String emailBody) {

    try {
      RestTemplate restTemplate = new RestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "Bearer " + getToken());

      String url = System.getenv("CHES_API_URL") + "/email";

      Map request = Map.of(
          "bodyType", "html",
          "body", emailBody,
          "from", "FSA_donotreply@gov.bc.ca",
          "subject", "Forest Client Application Confirmation",
          "to", List.of(emailTo.replaceAll("\\s", "").split(","))
      );

      HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);
      String response = restTemplate.postForObject(toURI(url), entity, String.class);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Failed to send email" + e.toString());
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }

}
