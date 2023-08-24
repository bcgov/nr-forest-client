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
    And I type in "3219 34 Ave SE Calgary" in the Street address
    And I select the Street address that contains "3219 34 Ave SE Calgary" from the list
    Then the Street address gets updated to "3219 34 Ave SE"
    And the City gets updated to "Calgary"
    And the Province gets updated to "Alberta"
    And the Postal code gets updated to "T2B2M6"
    And the button Next is enabled
    When I click the button Next
    Then I get to the Contacts tab
    When I select the Address name "Mailing Address"
    And I select the Primary role "Billing"
    And I type in "7804146040" as Phone number
    Then the button Next is enabled
    When I click the button Next
    Then I get to the Review tab
    And the displayed Business information match the provided information
    And the displayed Address information match the provided information
    And the displayed Contacts information match the provided information
