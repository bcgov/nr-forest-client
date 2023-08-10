Feature: Business Information: BC registered business: Abort application

  Background: List of business names is presented
    Given I navigate to the client application form
    When I select the option that says I have a BC registered business
    And I type in the first characters of the business name
    Then I am presented a list of business names filtered by the text typed in the business name field
    And the button Next is disabled

  Scenario: Business which is not in good standing
    Given I select the name of a business which is not in good standing from the filtered list
    And a notification for the business being 'Not in good standing' is displayed
    And the button 'End application and logout' is displayed
    When I click the button 'End application and logout'
    Then I am redirected to the landing page

  Scenario: Business which already has a client number
    Given I select the name of a business which already has a client number from the filtered list
    And a notification saying that 'Client already exists' is displayed
    And the button 'Receive email and logout' is displayed
    When I click the button 'Receive email and logout'
    Then I am redirected to the landing page
