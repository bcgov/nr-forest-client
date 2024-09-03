Feature: Check application login

  This validate the possibility of login with any kind of provider

  Scenario: Try to log with BCeID
    Given I visit "/"
    Then I cannot see the "Log in with BCeID" cds-button
    Then I see the "Log in with IDIR" cds-button
    Then I navigate to "/landing?fd_to=&ref=external"
    Then I can see the title "Log in with"

  Scenario: Try to log with BCSC
    Given I visit "/"
    Then I cannot see the "Log in with BC Services Card" cds-button
    Then I see the "Log in with IDIR" cds-button
    Then I navigate to "/landing?fd_to=&ref=individual"
    Then I can see the title "Continue with"
    Then I can see the title "BC Services Card app"
    Then I can see the title "Test with username and password"

  Scenario: Try to log with IDIR
    Given I visit "/"
    Then I can see the title "Client Management System"
    And I click on the "Log in with IDIR" button
    Then I can see the title "Log in with IDIR"
