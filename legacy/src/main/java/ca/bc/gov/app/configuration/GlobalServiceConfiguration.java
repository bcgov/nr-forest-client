package ca.bc.gov.app.configuration;

import ca.bc.gov.app.dto.ClientNameCodeDto;
import ca.bc.gov.app.dto.ForestClientDto;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@RegisterReflectionForBinding({
    ClientNameCodeDto.class,
    ForestClientDto.class
})
public class GlobalServiceConfiguration {
}
