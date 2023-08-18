Feature: Business Information: BC registered business: Abort application

  Background: List of business names is presented
    Given I navigate to the client application form
    And the button Next is disabled
    And I select the option that says I have a BC registered business

  Scenario: Business which is not in good standing
    Given I type in "bad" in the business name input
    And I select "BAD HORSE INC" from the filtered list of businesses
    Then a notification for the business being 'Not in good standing' is displayed
    And the button 'End application and logout' is displayed
    When I click the button 'End application and logout'
    Then I am redirected to the logout route

  Scenario: Business which already has a client number
    Given I type in "dupp" in the business name input
    And I select "DUPP MEDIA INC" from the filtered list of businesses
    Then a notification saying that 'Client already exists' is displayed
    And the button 'Receive email and logout' is displayed
    When I click the button 'Receive email and logout'
    Then I am redirected to the logout route
