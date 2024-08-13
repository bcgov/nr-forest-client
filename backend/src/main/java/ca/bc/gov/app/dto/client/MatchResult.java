package ca.bc.gov.app.dto.client;

public record MatchResult(
    String field,
    String match,
    boolean fuzzy,
    boolean partialMatch
) {
}
