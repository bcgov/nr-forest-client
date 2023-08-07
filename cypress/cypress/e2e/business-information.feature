Feature: Business Information

  Scenario: Initial state
    Given I navigate to the client application form
    Then the links for all the next steps presented in the breadcrumbs are all disabled
    And the button Next is disabled

  Scenario: Select 'BC registered business' as business type
    Given I navigate to the client application form
    When I select the option that says I have a BC registered business
    Then a field to type in the business name is displayed
    And a notification about 'Registered business name' is displayed
    And the button Next is disabled

  Scenario: Type in the beginning of the business name
    Given I navigate to the client application form
    When I select the option that says I have a BC registered business
    And I type in the first 3 characters of the business name
    Then I am presented a list of business names filtered by the text typed in the business name field
