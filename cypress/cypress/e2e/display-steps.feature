Feature: Steps are displayed correctly

  Test the page layout regarding the wizard steps according to different screen widths.

  Scenario: Steps are displayed horizontally
    Given I am on the form page
    And the width of screen is 672 pixels
    Then steps are displayed horizontally

  Scenario: Steps are displayed vertically
    Given I am on the form page
    And the width of screen is 671 pixels
    Then steps are displayed vertically