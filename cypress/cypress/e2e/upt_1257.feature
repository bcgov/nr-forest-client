Feature: Client Type Code

  #Ensure all allowable Client type codes are accepted
  
  Scenario: Submit BC registered business
  

  @loginAsEditor
  Scenario: Editor can submit registered LLP
    When I click on the "Create client" button
    And I can read "Create client"
    Then I select "BC registered business" from the "Client type" form input
    And I type "strucan" and select "STRUCAN LLP" from the "Client name" form autocomplete
    Then I wait for the text "This information is from BC Registries" to appear
    Then I click on next
    And I fill the "Primary location" address with the following
      | Field name                      | Value           | Type          |      
      | Street address or PO box        | 1015 Johnson    | autocomplete  |
      | Notes                           | This is a test  | textbox       |
    Then I click on next
    And I fill the "Primary contact" information with the following
      | Field name                      | Value           | Type          |  
      | First name                      | Testy           | text          | 
      | Last name                       | Tester          | text          |
      | Email address                   | mail5@mail.ca   | text          |
      | Primary phone number            | 7780000007      | text          |      
      | Contact type                    | Billing         | select        |
      | Associated locations            | Mailing address | multiselect   |
    And I click on next
    Then I submit
    And I wait for the text "has been created!" to appear
