package ca.bc.gov.app;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
    title = "${info.app.name}",
    version = "${info.app.version}",
    description = "${info.app.description}"
),
    servers = {
        @Server(url = "/", description = "Default Server URL")
    }
)
@EnableScheduling
public class LegacyApplication {

  public static void main(String[] args) {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    SpringApplication.run(LegacyApplication.class, args);
  }
}
