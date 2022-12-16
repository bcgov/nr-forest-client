package ca.bc.gov.app;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "${info.app.name}", version = "${info.app.version}", description = "${info.app.description}"))
public class BootApplication {

  public static void main(String[] args) {
    SpringApplication.run(BootApplication.class, args);
  }
}
