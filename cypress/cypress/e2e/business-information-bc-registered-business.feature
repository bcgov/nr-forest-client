Feature: Business Information: BC registered business

  Background: List of business names is presented
    Given I navigate to the client application form
    And the button Next is disabled
    And I select the option that says I have a BC registered business

  Scenario: Business in good standing
    When I type in "good" in the business name input
    And I select "GOOD SOFTWARE INC" from the filtered list of businesses
    Then the button Next is enabled

  Scenario: Business data not found in the BC Registries
    When I type in "missing" in the business name input
    And I select "MISSING LINK FENCE LTD" from the filtered list of businesses
    Then the button Next is enabled

  Scenario: Business which is not in good standing
    When I type in "bad" in the business name input
    And I select "BAD HORSE INC" from the filtered list of businesses
    Then a notification for the business being 'Not in good standing' is displayed
    And the button Next is hidden
    And the button 'End application and logout' is displayed

  Scenario: Business which already has a client number
    When I type in "dupp" in the business name input
    And I select "DUPP MEDIA INC" from the filtered list of businesses
    Then a notification saying that 'Client already exists' is displayed
    And the button Next is hidden
    And the button 'Receive email and logout' is displayed
