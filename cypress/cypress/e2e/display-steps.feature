Feature: Steps are displayed correctly

  Test the page layout regarding the wizard steps according to different screen widths.

  # screen width above or equal to 672
  Scenario Outline: Steps are displayed horizontally
    Given I am on the form page
    And the width of screen is <width> pixels
    Then steps are displayed horizontally

    Examples:
      | width |
      |   672 |
      |  1200 |

  # screen width below 672
  Scenario Outline: Steps are displayed vertically
    Given I am on the form page
    And the width of screen is <width> pixels
    Then steps are displayed vertically

    Examples:
      | width |
      |   671 |
      |   400 |
