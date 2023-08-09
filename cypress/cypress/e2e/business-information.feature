Feature: Business Information

  Scenario: Initial state
    Given I navigate to the client application form
    Then the links for all the next steps presented in the breadcrumbs are all disabled
    And the button Next is disabled
