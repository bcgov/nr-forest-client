Feature: Check application login

  This validate the possibility of login with any kind of provider

  Scenario: Try to log with BCeID
    Given I visit "/landing"
    Then I can read "Client Management System"
    Then I cannot see "Log in with BCeID"
    Then I can read "Log in with IDIR"
    Then I visit "/landing?fd_to=&ref=external"
    Then I can read "Log in with"

  Scenario: Try to log with BCSC
    Given I visit "/landing"
    Then I can read "Client Management System"
    Then I cannot see "Log in with BC Services Card"
    Then I can read "Log in with IDIR"
    Then I visit "/landing?fd_to=&ref=individual"
    Then I can read "Continue with"
    Then I can read "BC Services Card app"
    Then I can read "Test with username and password"

  Scenario: Try to log with IDIR
    Given I visit "/landing"
    Then I can read "Client Management System"
    And I click on the "Log in with IDIR" button
    Then I can read "Log in with IDIR"
