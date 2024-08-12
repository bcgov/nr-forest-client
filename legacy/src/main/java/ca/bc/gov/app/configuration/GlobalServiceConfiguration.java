package ca.bc.gov.app.configuration;

import ca.bc.gov.app.dto.AddressSearchDto;
import ca.bc.gov.app.dto.ClientDoingBusinessAsDto;
import ca.bc.gov.app.dto.ClientNameCodeDto;
import ca.bc.gov.app.dto.ContactSearchDto;
import ca.bc.gov.app.dto.ForestClientContactDto;
import ca.bc.gov.app.dto.ForestClientDto;
import ca.bc.gov.app.dto.ForestClientLocationDto;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@RegisterReflectionForBinding({
    ClientNameCodeDto.class,
    ForestClientDto.class,
    AddressSearchDto.class,
    ContactSearchDto.class,
    ClientDoingBusinessAsDto.class,
    ClientNameCodeDto.class,
    ForestClientContactDto.class,
    ForestClientLocationDto.class
})
public class GlobalServiceConfiguration {
}
