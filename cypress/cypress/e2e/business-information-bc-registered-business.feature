Feature: Business Information: BC registered business

  Background: List of business names is presented
    Given I navigate to the client application form
    And the button Next is disabled
    And I select the option that says I have a BC registered business

  # Scenario: Business in good standing
  #   When I type in "good soft" in the business name input
  #   And I select "GOOD SOFTWARE INC" from the filtered list of businesses
  #   Then the button Next is enabled

  Scenario: Business data not found in the BC Registries
    When I type in "missing link" in the business name input
    And I select "MISSING LINK FENCE LTD" from the filtered list of businesses
    Then the button Next is enabled
    When I click the button Next
    Then I get to the Address tab
    And the button Next is disabled
    When the list of countries finishes loading
    And I type in "3219 34 Ave SE Calgary" in the Street Address
    And I select the Street Address that contains "3219 34 Ave SE Calgary" from the list
    Then the City gets updated to "Calgary"
    And the Province gets updated to "Alberta"
    And the Postal code gets updated to "T2B 2M6"
    And the button Next is enabled
    When I click the button Next
    Then I get to the Contacts tab
    When I select the Address name "Mailing Address"
