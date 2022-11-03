package ca.bc.gov.app.core.configuration;

import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

	@Bean
	public Docket applicationApi() {
		return new Docket(DocumentationType.SWAGGER_2)
					.apiInfo(apiInfo())
					.select()
					.apis(RequestHandlerSelectors.basePackage("ca.bc.gov.app"))
					.paths(regex("/app/m/.*"))
					.build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
					.title("FSA Forest Client Rest APIs")
					.description("APIs that allows systems to consume forest client data.")
					.version("1.0")
					.build();
	}
}