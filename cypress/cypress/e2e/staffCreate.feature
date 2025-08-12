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
    Then I click on next
    And I fill the "Primary location" address with the following
      | Field name                      | Value           | Type          |
      | Street address or PO box        | 1520 Blanshard St | autocomplete  |    
    Then I click on next
    And I fill the "Primary contact" information with the following
      | Field name                      | Value           | Type          |      
      | Email address                   | baxter.james@mail.ca    | text          |
      | Primary phone number            | 2501237567      | text          |
      | Secondary phone number          | 2501232567      | text          |
      | Fax                             | 2501444567      | text          |
      | Contact type                    | Billing         | select        |
      | Associated locations            | Mailing address | multiselect   |
    Then I click on next
    Then I submit
    And I wait for the text "has been created!" to appear

  @loginAsEditor
  Scenario: Editor can submit registered
    When I click on the "Create client" button
    And I can read "Create client"
    Then I select "BC registered business" from the "Client type" form input
    And I type "star dot star" and select "STAR DOT STAR VENTURES" from the "Client name" form autocomplete
    Then I wait for the text "This information is from BC Registries" to appear
    Then I click on next
    And I fill the "Primary location" address with the following
      | Field name                      | Value           | Type          |      
      | Street address or PO box        | 1515 Blanshard  | autocomplete  |
      | Notes                           | This is a test  | textbox       |
    And I add a new location called "Home"
    And I fill the "Home" address with the following
      | Field name                      | Value           | Type          |      
      | Street address or PO box        | 1515 Blanshard  | autocomplete  |
    Then I click on next
    And I fill the "Primary contact" information with the following
      | Field name                      | Value           | Type          |      
      | Email address                   | mail3@mail.ca   | text          |
      | Primary phone number            | 7780000003      | text          |      
      | Contact type                    | Billing         | select        |
      | Associated locations            | Home            | multiselect   |
      | Associated locations            | Mailing address | multiselect   |
    And I fill the "MARCEL ST. AMANT" information with the following
      | Field name                      | Value           | Type          |      
      | Email address                   | mail3@mail.ca   | text          |
      | Primary phone number            | 7780000004      | text          |
      | Contact type                    | Billing         | select        |
      | Associated locations            | Home            | multiselect   |
      | Last name                       | ST AMANT        | textreplace   |
    And I click on next
    Then I submit
    And I wait for the text "has been created!" to appear

  @loginAsEditor
  Scenario: Already exists and has fuzzy match
    When I click on the "Create client" button
    And I can read "Create client"
    Then I select "Individual" from the "Client type" form input
    And I fill the form as follows
      | Field name | Value                     | Type   |
      | First name | James                     | text   |
      | Last name  | Baxter                    | text   |
      | Year       | 1959                      | text   |
      | Month      | 05                        | text   |
      | Day        | 18                        | text   |
      | ID type    | Canadian driver's licence | select |
      | ID number  | 1234567                   | text   |
    Then I click on next
    And I should see the "warning" message "was found with similar name and birthdate" on the "top"
    And The field "First name" should have the "warning" message "There's already a client with this name"
