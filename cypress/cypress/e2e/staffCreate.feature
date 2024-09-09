Feature: Staff Screens

  This feature file is to test staff screen use cases

  
  @loginAsEditor
  Scenario: Submit individuals
    When I click on the "Create client" button
    And I can read "Create client"
    Then I select "Individual" from the "Client type" form input
    And I fill the form as follows
      | Field name | Value                     | Type   |
      | First name | James                     | text   |
      | Last name  | Baxter                    | text   |
      | Year       | 1990                      | text   |
      | Month      | 01                        | text   |
      | Day        | 01                        | text   |
      | ID type    | Canadian driver's licence | select |
      | ID number  | 4417845                   | text   |
    Then I click on the "Next" button
    And I fill the "Primary location" address with the following
      | Field name                      | Value           | Type          |
      | Location name                   | Office          | text          |
      | Street address or PO box        | 2975 Jutland Rd | autocomplete  |
      | Email address                   | mail@mail.ca    | text          |
      | Primary phone number            | 2501234567      | text          |
      | Secondary phone number          | 2501234567      | text          |
      | Fax                             | 2501234567      | text          |
      | Notes                           | This is a test  | textbox       |
    Then I add a new location called "Home"
    And I fill the "Home" address with the following
      | Field name                      | Value           | Type          |
      | Street address or PO box        | 2975 Jutland Rd | autocomplete  |
    Then I click on the "Next" button
    And I fill the "Primary contact" information with the following
      | Field name                      | Value           | Type          |      
      | Email address                   | mail@mail.ca    | text          |
      | Primary phone number            | 2501234567      | text          |
      | Secondary phone number          | 2501234567      | text          |
      | Fax                             | 2501234567      | text          |
      | Contact type                    | Billing         | select        |
      | Location name                   | Office          | multiselect   |
    And I click on the "Next" button
    Then I submit
    And I wait for the text "has been created!" to appear

  @loginAsEditor
  Scenario: Editor can submit registered
    When I click on the "Create client" button
    And I can read "Create client"
    Then I select "BC registered business" from the "Client type" form input
    And I type "star dot star" and select "STAR DOT STAR VENTURES" from the "Client name" form autocomplete
    Then I wait for the text "This information is from BC Registries" to appear
    Then I click on the "Next" button
    And I fill the "Primary location" address with the following
      | Field name                      | Value           | Type          |
      | Location name                   | Office          | text          |
      | Street address or PO box        | 2970 Jutland Rd | autocomplete  |
      | Email address                   | mail2@mail.ca    | text          |
      | Primary phone number            | 2501234568      | text          |
      | Secondary phone number          | 2501234568      | text          |
      | Fax                             | 2501234568      | text          |
      | Notes                           | This is a test  | textbox       |
    Then I add a new location called "Home"
    And I fill the "Home" address with the following
      | Field name                      | Value           | Type          |
      | Street address or PO box        | 2970 Jutland Rd | autocomplete  |
    Then I click on the "Next" button
    And I fill the "Primary contact" information with the following
      | Field name                      | Value           | Type          |      
      | Email address                   | mail3@mail.ca    | text          |
      | Primary phone number            | 2501234568      | text          |
      | Secondary phone number          | 2501234568      | text          |
      | Fax                             | 2501234568      | text          |
      | Contact type                    | Billing         | select        |
      | Location name                   | Office          | multiselect   |
    And I click on the "Next" button
    Then I submit
    And I wait for the text "has been created!" to appear
