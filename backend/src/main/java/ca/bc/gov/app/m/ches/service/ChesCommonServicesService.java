package ca.bc.gov.app.m.ches.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChesCommonServicesService {

  public static final Logger logger = LoggerFactory.getLogger(ChesCommonServicesService.class);

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
      logger.error("Failed to get email authentication token" + e.toString());
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

      JSONObject request = new JSONObject();
      request.put("bodyType", "html");
      request.put("body", emailBody);
      request.put("from", "FSA_donotreply@gov.bc.ca");
      request.put("subject", "Forest Client Application Confirmation");
      List<String> emailToList = List.of(emailTo.replaceAll("\\s", "").split(","));
      request.put("to", emailToList);

      HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);
      String response = restTemplate.postForObject(toURI(url), entity, String.class);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("Failed to send email" + e.toString());
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

  }

}
