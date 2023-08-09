Feature: Business Information: BC registered business

  Background: List of business names is presented
    Given I navigate to the client application form
    When I select the option that says I have a BC registered business
    And I type in the first 3 characters of the business name
    Then I am presented a list of business names filtered by the text typed in the business name field
    And the button Next is disabled

  Scenario: Business in good standing
    When I select the name of a business in good standing from the filtered list
    Then the button Next is enabled

  Scenario: Business which is not in good standing
    When I select the name of a business which is not in good standing from the filtered list
    Then a notification for the business being 'Not in good standing' is displayed
    And the button Next is hidden
    And the button 'End application and logout' is displayed

  Scenario: Business which already has a client number
    When I select the name of a business which already has a client number from the filtered list
    Then a notification saying that 'Client already exists' is displayed
    And the button Next is hidden
    And the button 'Receive email and logout' is displayed
