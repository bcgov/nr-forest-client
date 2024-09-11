Feature: BceID User Tests

  @loginAsBCeID
  Scenario: BceID User Logout
    Given I am a "BceID" user
    When I can read "New client application"
    Then I click on the "Logout" button
    And I can read "Are you sure you want to logout? Your data will not be saved."
    Then I click on the "Logout" button
