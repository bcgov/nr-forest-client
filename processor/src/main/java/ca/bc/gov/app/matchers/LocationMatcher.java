package ca.bc.gov.app.matchers;

import static java.util.function.Predicate.not;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.app.dto.MatcherResult;
import ca.bc.gov.app.dto.SubmissionInformationDto;
import ca.bc.gov.app.dto.legacy.ForestClientLocationDto;
import ca.bc.gov.app.repository.SubmissionLocationRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LocationMatcher implements ProcessorMatcher {

	private final WebClient legacyClientApi;
	private final SubmissionLocationRepository locationRepository;

	public LocationMatcher(
			@Qualifier("legacyClientApi") WebClient legacyClientApi, 
			SubmissionLocationRepository locationRepository) {
		super();
		this.legacyClientApi = legacyClientApi;
		this.locationRepository = locationRepository;
	}

	@Override
	public boolean enabled(SubmissionInformationDto submission) {
		return true;
	}

	@Override
	public String name() {
		return "Location Matcher";
	}

	@Override
	public Mono<MatcherResult> matches(SubmissionInformationDto submission) {
		
		log.info("{} :: Validating {}", name(), submission.corporationName());

		return locationRepository.findBySubmissionId(submission.submissionId())
				.flatMap(location -> legacyClientApi
				.get()
				.uri(
					uriBuilder -> 
						uriBuilder
							.path("/api/locations/search")
							.queryParam("address", location.getStreetAddress())
							.queryParam("postalCode", location.getPostalCode())
							.build()
					)
					.exchangeToFlux(response -> response.bodyToFlux(ForestClientLocationDto.class))
				)
				.map(ForestClientLocationDto::clientNumber)
				.collectList()
				.filter(not(List::isEmpty))
				.map(values -> 
						new MatcherResult("location", String.join(",", values)));
	}

}
