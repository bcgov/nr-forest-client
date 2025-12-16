Feature: BceID User Tests

  Deals with BCeID Business user scenarios

  @loginAsBCeID
  Scenario: BceID User Login
    Given I am a "BceID" user    
    When I can read "New client application"

  @loginAsBCeID
  Scenario: BceID Registered User
    Given I am a "BceID" user
    When I can read "New client application"
    And I select "DMH - 100 Mile House Natural Resource District" from the "District" form input
    And I type "TONY BHAY" and select "TONY BHAYANA" from the "BC registered business name" form autocomplete
    And I fill the form as follows
      | Field name | Value                     | Type   |      
      | Year       | 1990                      | text   |
      | Month      | 01                        | text   |
      | Day        | 01                        | text   |
    Then I click on next    
    And I click on next
    And I type "2255522552" into the "Phone number" form input
    Then I select "Billing" from the "Primary role" form input
    And I fill the "PARMJIT BHAYANA" information with the following
      | Field name      | Value              | Type          |
      | Email address   | parmjitbha@mail.ca | text          |
      | Phone number    | 7787787078         | text          |      
      | Primary role    | Director           | select        |
    And I click on next
    Then I submit
    And I can read "Application submitted!"
