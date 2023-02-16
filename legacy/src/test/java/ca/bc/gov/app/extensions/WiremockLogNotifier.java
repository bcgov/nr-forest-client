package ca.bc.gov.app.extensions;

import com.github.tomakehurst.wiremock.common.Notifier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WiremockLogNotifier implements Notifier {

  @Override
  public void info(String message) {
    log.info(message);
  }

  @Override
  public void error(String message) {
    log.error(message);
  }

  @Override
  public void error(String message, Throwable t) {
    log.error(message, t);
  }
}
