Feature: BceID User Tests

  Deals with BCeID Business user scenarios

  @loginAsBCeID
  Scenario: BceID User Login
    Given I am a "BceID" user    
    When I can read "New client application"
    
  @loginAsBCeID
  Scenario: BceID Unregistered User
    Given I am a "BceID" user
    When I can read "New client application"
    And I select "DMH - 100 Mile House Natural Resource District" from the "District" form input
    Then I mark "I have an unregistered sole proprietorship" on the "Type of business" "radio" input
    And I fill the form as follows
      | Field name | Value                     | Type   |      
      | Year       | 1990                      | text   |
      | Month      | 01                        | text   |
      | Day        | 01                        | text   |
    Then I click on next
    And I type "2975 Jutland Rd" and select "2975 Jutland Rd" from the "Street address or PO box" form autocomplete
    Then I click on next
    And I type "2255522552" into the "Phone number" form input
    Then I select "Billing" from the "Primary role" form input
    And I add a new contact called "John Wick"
    And I fill the "John Wick" information with the following
      | Field name    | Value                   | Type   |
      | Email address | jwick@thecontinental.ca | text   |
      | Phone number  | 2501234568              | text   |
      | Primary role  | Director                | select |
    And I click on next
    Then I submit
    And I can read "Application submitted!"

  @loginAsBCeID
  Scenario: BceID Registered User
    Given I am a "BceID" user
    When I can read "New client application"
    And I select "DMH - 100 Mile House Natural Resource District" from the "District" form input
    Then I mark "I have a BC registered business (corporation, sole proprietorship, society, etc.)" on the "Type of business" "radio" input
    And I type "SOLE TO" and select "SOLE TO SOLE BLENDED REFLEXOLOGY" from the "BC registered business name" form autocomplete
    And I fill the form as follows
      | Field name | Value                     | Type   |      
      | Year       | 1990                      | text   |
      | Month      | 01                        | text   |
      | Day        | 01                        | text   |
    Then I click on next
    And I type "2975 Jutland Rd" and select "2975 Jutland Rd" from the "Street address or PO box" form autocomplete    
    Then I click on next
    And I type "2255522552" into the "Phone number" form input
    Then I select "Billing" from the "Primary role" form input
    And I fill the "GARY VEITCH" information with the following
      | Field name      | Value              | Type          |
      | Email address   | garyveitch@mail.ca | text          |
      | Phone number    | 7787787778         | text          |      
      | Primary role    | Director           | select        |
    And I click on next
    Then I submit
    And I can read "Application submitted!"

  @loginAsBCeID
  Scenario: BceID Unregistered User already registered
    Given I am a "BceID" user
    When I can read "New client application"
    And I select "DMH - 100 Mile House Natural Resource District" from the "District" form input
    Then I mark "I have an unregistered sole proprietorship" on the "Type of business" "radio" input
    And I should see the "error" message "Looks like “01-DEV, LOAD” has a client number. Select the 'Receive email and logout' button below to have it sent to you at maria.martinez@gov.bc.ca" on the "Business information"
