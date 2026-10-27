package ca.bc.gov.app.predicates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.bc.gov.app.models.client.SubmissionStatusEnum;
import ca.bc.gov.app.models.client.SubmissionTypeCodeEnum;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.relational.core.query.Criteria;

@DisplayName("Unit Test | Submission Predicates")
class SubmissionPredicatesTest {

  @ParameterizedTest
  @MethodSource("orType")
  @DisplayName("CASE 7: Should check orType")
  void shouldCheckOrType(SubmissionTypeCodeEnum[] values, Criteria expected) {
    assertTrue(
        areCriteriaEqual(
            expected,
            SubmissionPredicates.orType(values)
        )
    );
  }
  @ParameterizedTest
  @MethodSource("orStatus")
  @DisplayName("CASE 1: Should check orStatus")
  void shouldCheckOrStatus(SubmissionStatusEnum[] values, Criteria expected) {
    assertTrue(
        areCriteriaEqual(
            expected,
            SubmissionPredicates.orStatus(values)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("orSubmittedAt")
  @DisplayName("CASE 2: Should check orSubmittedAt")
  void shouldCheckOrUpdateAt(String[] values, Criteria expected) {
    assertTrue(
        areCriteriaEqual(
            expected,
            SubmissionPredicates.orSubmittedAt(values)
        )
    );
  }

  @ParameterizedTest
  @MethodSource("orContains")
  @DisplayName("CASE 3: Should check orContains")
  void shouldCheckOrContains(String[] values, Criteria expected) {
    assertTrue(
        areCriteriaEqual(
            expected,
            QueryPredicates.orContains(values, "submissionId")
        )
    );
  }

  @ParameterizedTest
  @MethodSource("orEqualTo")
  @DisplayName("CASE 4: Should check orEqualTo")
  void shouldCheckOrEqualTo(String[] values, Criteria expected) {
    assertTrue(
        areCriteriaEqual(
            expected,
            QueryPredicates.orEqualTo(values, "submissionId")
        )
    );
  }

  @ParameterizedTest
  @MethodSource("isBefore")
  @DisplayName("CASE 5: Should check isBefore")
  void shouldCheckIsBefore(LocalDateTime value, Criteria expected) {
    assertTrue(
        areCriteriaEqual(
            expected,
            QueryPredicates.isBefore(value, "submittedAt")
        )
    );
  }

  @ParameterizedTest
  @MethodSource("isAfter")
  @DisplayName("CASE 6: Should check isAfter")
  void shouldCheckIsAfter(LocalDateTime value, Criteria expected) {
    assertTrue(
        areCriteriaEqual(
            expected,
            QueryPredicates.isAfter(value, "submittedAt")
        )
    );
  }

  private static Stream<Arguments> orType() {
    return Stream.of(
        Arguments.of(
            null, Criteria.empty()
        ),
        Arguments.of(
            new SubmissionTypeCodeEnum[0], Criteria.empty()
        ),
        Arguments.of(
            new SubmissionTypeCodeEnum[]{null}, Criteria.empty()
        ),
        Arguments.of(
            new SubmissionTypeCodeEnum[]{SubmissionTypeCodeEnum.AAC},
            Criteria.where("submissionType").is(SubmissionTypeCodeEnum.AAC)
        )
    );
  }
  private static Stream<Arguments> orStatus() {
    return Stream.of(
        Arguments.of(
            null, Criteria.empty()
        ),
        Arguments.of(
            new SubmissionStatusEnum[0], Criteria.empty()
        ),
        Arguments.of(
            new SubmissionStatusEnum[]{null}, Criteria.empty()
        ),
        Arguments.of(
            new SubmissionStatusEnum[]{SubmissionStatusEnum.A},
            Criteria.where("submissionStatus").is(SubmissionStatusEnum.A)
        )
    );
  }

  private static Stream<Arguments> orSubmittedAt() {
    return Stream.of(
        Arguments.of(
            null, Criteria.empty()
        ),
        Arguments.of(
            new String[0], Criteria.empty()
        ),
        Arguments.of(
            new String[]{null, "", "  "}, Criteria.empty()
        ),
        Arguments.of(
            new String[]{"2020-01-01"},
            Criteria.where("submittedAt")
                .lessThanOrEquals(LocalDateTime.of(2020, 1, 2, 0, 0))
        )

    );
  }

  private static Stream<Arguments> orContains() {
    return Stream.of(
        Arguments.of(
            null, Criteria.empty()
        ),
        Arguments.of(
            new String[0], Criteria.empty()
        ),
        Arguments.of(
            new String[]{null, "", "  "}, Criteria.empty()
        ),
        Arguments.of(
            new String[]{"123"},
            Criteria.where("submissionId").like("123").ignoreCase(true)
        )

    );
  }

  private static Stream<Arguments> orEqualTo() {
    return Stream.of(
        Arguments.of(
            null, Criteria.empty()
        ),
        Arguments.of(
            new String[0], Criteria.empty()
        ),
        Arguments.of(
            new String[]{null, "", "  "}, Criteria.empty()
        ),
        Arguments.of(
            new String[]{"123"},
            Criteria.where("submissionId").is("123")
        )

    );
  }

  private static Stream<Arguments> isAfter() {
    return Stream.of(
        Arguments.of(
            null, Criteria.empty()
        ),
        Arguments.of(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            Criteria.where("submittedAt")
                .greaterThanOrEquals(LocalDateTime.of(2020, 1, 1, 0, 0))
        )

    );
  }

  private static Stream<Arguments> isBefore() {
    return Stream.of(
        Arguments.of(
            null, Criteria.empty()
        ),
        Arguments.of(
            LocalDateTime.of(2020, 1, 1, 0, 0),
            Criteria.where("submittedAt")
                .lessThanOrEquals(LocalDateTime.of(2020, 1, 1, 0, 0))
        )

    );
  }

  private static boolean areCriteriaEqual(Criteria target, Criteria result) {

    assertEquals(target.getColumn(), result.getColumn());
    assertEquals(target.getComparator(), result.getComparator());
    assertEquals(target.getValue(), result.getValue());
    return true;
  }

}