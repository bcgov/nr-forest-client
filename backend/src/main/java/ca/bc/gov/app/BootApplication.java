package ca.bc.gov.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ca.bc.gov.app.job.ches.ChesEmailResendJob;

@SpringBootApplication
public class BootApplication {

  public static void main(String[] args) {
    SpringApplication.run(BootApplication.class, args);
  }
  
  @Bean
  public CommandLineRunner schedulerRunner(ChesEmailResendJob chesEmailResendJob) {
      return args -> chesEmailResendJob.startResendJob();
  }
  
}
