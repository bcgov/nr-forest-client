package ca.bc.gov.app.integration;

import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.entity.client.SubmissionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SampleIntegration {


  @ServiceActivator(inputChannel = "autoApproveChannel")
  public void approved(Message<SubmissionInformationDto> eventMono) {
    log.info("Your request was approved {}",eventMono);
  }

  @ServiceActivator(inputChannel = "reviewChannel")
  public void reviewed(Message<SubmissionInformationDto> eventMono) {
    log.info("Oops, needs review {}",eventMono);
  }

}
