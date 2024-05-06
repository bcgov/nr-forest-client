package ca.bc.gov.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ProcessorApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProcessorApplication.class, args);
  }

}
