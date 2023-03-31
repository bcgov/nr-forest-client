package ca.bc.gov.app;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
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
    SpringApplication.run(LegacyApplication.class, args);
  }

}
