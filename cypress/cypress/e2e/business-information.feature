Feature: Business Information

  Scenario: Initial state
    Given I navigate to the client application form
    Then the links for all the next steps presented in the breadcrumbs are all disabled
    And the button Next is disabled

  Scenario: Unregistered sole proprietorship
    Given I navigate to the client application form
    When I select the option that says I have an unregistered sole proprietorship
    Then my name is displayed under the label 'Unregistered proprietorship'
    And the button Next is enabled