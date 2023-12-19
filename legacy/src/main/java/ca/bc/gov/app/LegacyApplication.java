package ca.bc.gov.app;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LegacyApplication {

  public static void main(String[] args) {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    SpringApplication.run(LegacyApplication.class, args);
  }

}
