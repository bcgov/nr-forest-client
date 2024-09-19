# Automated End-to-End User journey tests with Cypress

This repository deals with automated user journey tests. We use cypress for automation and Cucumber and Gherking as the framework to describe the scenarios.
The idea behind this is to allow a colaborative environment where anyone can write down a set operations that composes a Journey to be tested, and the developers will make sure to implement it.

The main objective is to cover as many cases as possible by allowing non developers to describe scenarios that sometimes are forgotten by the development team and allow different point of view for journeys.

The below sections are intended to explain how the framework works, along with a few instructions. Please make sure to read it carefuly and don't be afraid to play around.

## Introduction to Writing Cucumber/Gherkin Files for End-to-End Tests

[Cucumber](https://cucumber.io/) is a popular tool used for behavior-driven development (BDD) in software testing. It allows you to write executable specifications in a natural language format called Gherkin. Gherkin is a plain-text language that is easy to understand and can be used by non-technical stakeholders as well. In this tutorial, we will guide you through the basics of writing a Cucumber/Gherkin file for end-to-end tests. To learn more about Cucumber, Gherking and BDD, you can enroll on a [free course](https://school.cucumber.io/courses/bdd-overview-for-business-analysts-and-product-owners).

It is always good to read about Gherkin language format before start but in summary, Gherkin is a writing format that leverages plain english to allow users to describe the expected behavior in simple steps that is simple to understand by non-technical persons, but also makes sense to be executed in that specific order. Think of it like a set of steps in a cake recipe.

To make things easier for non-technical team members, we developed a strategy to leverage the use of [github issues](https://github.com/bcgov/nr-forest-client/issues), something that is quite similar to Jira Tickets. Click on `New issue` and a list of possible issue types will appear, select the `User provided automated test-case` one, and a form will appear. Follow the instructions on it on how to fill up the form with the appropriate data. This will then be automatically converted to a `feature file` that will be used for test.

    Pay attention to the format of the test description. Gherkin feature files are sensitive to spacing and the keywords are really picky when it comes to casing (uppercase x lowercase).

Another thing that the development team did to facilitate the usage of Gherkin is the ready-to-use collection of instructions that can be used to speed up the writing of test cases. Check the [existing step instructions](#existing-step-instructions) topic for a list of steps already implemented.

Without further ado, let's get started:

### Creating a Feature

Every test group is called a `Feature` on Gherkin. This is the first keyword here and it will have a meaningful name. This will contain your scenarios and it can include a short description of the feature you are testing, preceded by the Feature keyword. For example:

```gherkin
Feature: User Registration
  As a new user I want to register on the website so that I can access exclusive content
```

    Be aware that the description should be a level deeper than the Feature itself. You can use tab or two spaces

### Creating Scenarios

Scenarios represent specific test cases. Each scenario consists of a series of steps. Steps can be one of the following: **Given**, **When**, **Then**, **And**, or **But**. Here's an example scenario for our user registration feature:

```gherkin
Scenario: Successful user registration
  Given I am on the registration page
  When I fill in the registration form with valid information
  And I click the "Register" button
  Then I should see a success message
```

    Be aware that each step should be a level deeper than the Scenario itself. You can use tab or two spaces
    Also, keep in mind that this should be at least one level deeper than the feature above it.

Here's the final product of the above feature and scenario combined.

```gherkin
Feature: User Registration
  As a new user I want to register on the website so that I can access exclusive content
    
  Scenario: Successful user registration
    Given I am on the registration page
    When I fill in the registration form with valid information
    And I click the "Register" button
    Then I should see a success message
```

Congratulations! You have successfully written a basic Cucumber/Gherkin end-to-end tests. You can continue adding more scenarios and step definitions to cover different test cases in the application.

Remember, the power of Cucumber lies in its ability to bridge the communication gap between technical and non-technical team members, enabling collaboration and providing a common language for defining software behavior.

### Existing step instructions

The development team created a set of pre-defined step definitions that can be used to leverage the use of Gherkin tests and speed up the adoption of it with non-technical team members. The below steps should be used `as-is`, without changing the case of any letter, except for `variables`.

A variable is a piece of information that will be used to pass down a information. Every variable should be wrapped in double quotes (`"`). We will have two distinc group of variables described below, and they will be defined as `input` and `field name`. **Input** variables are the actual data that you want to select or insert in the form, such as the first name `James` or the `Individual` type of user. **Field name** is a type of variable used to identify a field in the form based on it's label name. Some examples are `First name` and `Client type`. They should have the exact name and casing as the form, otherwise the test won't be able to find the input to fill the data in.

    The below list of instructions can be used in any of the steps, such as `Given`, `When`, `Then`, `And` or `But`. They are case sensitive and should be used as they are.

Also, to speed up the process and avoid credentials being leaked everywhere, we have a special instruction that will be used to login using some specific user types. This instruction uses the `annotation` and it should be used at the `scenario` level. For more information, please refer to the [credentials](#credentials) topic below.

Here's a list of instructions that are already implemented and can be used:

| Step            | Variables          | Description                     | Example            |
| --------------- | ------------------ | ------------------------------- | ------------------ |
| I visit {input} | `input` as the URL | Navigate to a specific URL path | I visit "/landing" |
| I can read {input} | `input` as the text on the screen | Look up for a specific text on the screen | I can read "Create new client" |
| I cannot see {input} | `input` as the content of something that should not be on the screen | Look up for a specific text or component that should not be on the screen | I cannot see "Error" |
| I wait for the text {input} to appear | `input` as the text to be waited to appear on the screen | Wait for a specific text to appear on the screen | I wait for the text "Success" to appear |
| I click on the {field name} button | `field name` as the text/name of the button to be clicked | Finds and click a button | I click on the "Next" button |
| I click on next | - | Click on the next button. It is a variation of the button click, with a limited scope | I click on next |
| I submit | - | Submit the form at the end. Is a variation of the button click, but with a more limited scope | I submit |
| I type {input} into the {field name} form input | `input` as the data to be inserted and `field name` as the field name, based on a label | Insert data into a input text field | I type "James" into the "First name" form input |
| I clear the {field name} form input | `field name` as the field name, based on a label | Clear the content of a input text field | I clear the "First name" form input |
| I type {input} into the {field name} form input for the {field name} | `input` as the data to be inserted and `field name` as the field name, based on a label and the last `field name` is a reference for the group where this information should be inserted. | Insert data into a input text field that belongs to a specific group | I type "James" into the "First name" form input for the "Primary contact" |
| I replace the {field name} with {input} form input | `field name` as the field name, based on a label and `input` as the data to be inserted | Replace the content of a input text field | I replace the "First name" with "John" form input |
| I replace the {field name} with {input} form input for the {field name} | `field name` as the field name, based on a label and `input` as the data to be inserted and the last `field name` is a reference for the group where this information should be inserted. | Replace the content of a input text field | I replace the "First name" with "John" form input |
| I type {input} into the {field name} form input area | `input` as the data to be inserted and `field name` as the field name, based on a label | Insert data into a input area | I type "All good" into the "Notes" form input area |
| I clear the {field name} form input area | `field name` as the field name, based on a label | Clear the content of a input area | I clear the "First name" form input area |
| I type {input} into the {field name} form input area for the {field name} | `input` as the data to be inserted and `field name` as the field name, based on a label and the last `field name` is a reference for the group where this information should be inserted. | Insert data into a input area | I type "All good" into the "Notes" form input area for the "Primary contact" |
| I select {input} from the {field name} form input | `input` as the data to be selected and `field name` as the field name, based on a label | Select a option from a dropdown | I select "Individual" from the "Client type" form input |
| I select {input} from the {field name} form input for the {field name} | `input` as the data to be selected and `field name` as the field name, based on a label and the last `field name` is a reference for the group where this information should be inserted.  | Select a option from a dropdown | I select "Billing" from the "Client type" form input for the "Primary contact" |
| I select {input} from the {field name} multiselect | `input` as the data to be selected and `field name` as the field name, based on a label | Select a option from a multi select dropdown | I select "Individual" from the "Contact type" multiselect |
| I select {input} from the {field name} multiselect for the {field name} | `input` as the data to be selected and `field name` as the field name, based on a label and the last `field name` is a reference for the group where this information should be inserted. | Select a option from a multi select dropdown | I select "Office" from the "Location name" multiselect for the "Primary contact|
| I type {input} and select {input} from the {field name} form autocomplete | `input` as the data to be inserted, being the first one the text to be typed and the second one the text to be selected and `field name` as the field name, based on a label | Type into the autocomplete and selects one of the possible results | I type "James" and select "James Bond" from the "Client name" form autocomplete |
| I type {input} and select {input} from the {field name} form autocomplete for the {field name} | `input` as the data to be inserted, being the first one the text to be typed and the second one the text to be selected and `field name` as the field name, based on a label and the last `field name` is a reference for the group where this information should be inserted. | Type into the autocomplete and selects one of the possible results | I type "2975 Jutl" and select "2975 Jutland Rd" from the "Street address or PO box" form autocomplete for the "Primary location |
| I add a new location called {input} | `input` as the name of the location to be added | Add a new location to the list | I add a new location called "New York Office" |
| I addd a new contact called {input} | `input` as the name of the contact to be added | Add a new contact to the list | I add a new contact called "Johnathan Wick" |
| I should see the {input} message {input} on the {field name} | `input` as the type of message, being **error** or **warning**, `input` as the text of the message (can be partial) and `field name` as the location/group where the notification should appear, such as the **top**, **Primary contact** or **Office** | Check if a specific message is displayed on the screen | I should see the "error" message "Invalid email" on the "top" |
| The field {field name} should have the {input} message {input} | `field name` as the field name, based on a label, `input` as the type of message, being **error** or **warning**, `input` as the text of the message (can be partial) | Check if a specific message is displayed on a specific field | The field "Email" should have the "error" message "Invalid email" |
| I fill the form as follows | - | This is a special instruction that will be followed by a table with the data to be inserted in the form. More on the [data tables](#data-tables) topic below | I fill the form as follows |
| I fill the {input} address with the following | `input` as the location name value, or `Primary location` for the primary location | This is a special instruction that will be followed by a table with the data to be inserted in the form. More on the [data tables](#data-tables) topic below | I fill the "Primary location" address with the following |
| I fill the {input} information with the following | `input` as the location or contact name value, or `Primary contact` for the primary contact | This is a special instruction that will be followed by a table with the data to be inserted in the form. More on the [data tables](#data-tables) topic below | I fill the "Johnathan Wick" information with the following |
| I mark {input} on the {field name} {input} input | `input` as the value to be marked, `field name` as the field name, based on a label and the last `input` as the type of the input, being **radio** or **checkbox** | Mark a specific input | I mark "Yes" on the "Primary contact" "radio" input |
| The {field name} component is using the font {input} for the {field name} | `field name` as the field name, based on a label and `input` as the font name and the last `field name` is a reference for the group where this information should be inserted. | Check if a specific font is being used on a component | The "Business information" component is using the font "Arial" |
| The {input} has weight {input} inside the {field name} | `input` as the text to be checked, `input` as the font weight and `field name` as the location/group where the text should be | Check if a specific font weight is being used on a text | The "Business information" has weight "bold" inside the "form" |
| The {input} size is {input} inside the {field name} | `input` as the text to be checked, `input` as the font size and `field name` as the location/group where the text should be | Check if a specific font size is being used on a text | The "Business information" size is "16px" inside the "form" |

## For Developers

Each test is written in a file with the `.feature` extension and is saved inside [cypress/e2e](cypress/e2e) folder on this repo, you can check some of the existing files for reference. The developer will implement one `.ts` file for each `.feature` file, using the same name. Ex: `sample.feature` will have a corresponding `sample.ts` file. This `.ts` file is only required if the `.feature` file has any instruction that is too specific to be implemented as a common step verbate.

    Avoid using names with spaces or special characters. Replace spaces with `_` and special characters for it's non-special character counterpart

### Creating a Feature File

Create a new file with the `.feature` extension inside the [cypress/e2e](cypress/e2e) folder. This file will contain your Gherkin scenarios. Start by writing a short description of the feature you are testing, preceded by the Feature keyword. For example:

```gherkin
Feature: User Registration
  As a new user
  I want to register on the website
  So that I can access exclusive content
```

### Writing Scenarios

Scenarios represent specific test cases. Each scenario consists of a series of steps. Steps can be one of the following: **Given**, **When**, **Then**, **And**, or **But**. Here's an example scenario for our user registration feature:

```gherkin
Scenario: Successful user registration
  Given I am on the registration page
  When I fill in the registration form with valid information
  And I click the "Register" button
  Then I should see a success message
```

### Writing Step Definitions

Step definitions are the code implementation of the Gherkin steps. They define the actions to be taken for each step. For example, the step "I am on the registration page" could be implemented as follows in your programming language:

```gherkin
Given('I am on the registration page', () => {
  // Code to navigate to the registration page
});
```

### Running the Tests

Once you have defined your scenarios and step definitions, you can run your Cucumber tests. To run, execute one of the commands, depending on your need:

For an interface-guided execution, run:

```bash
npm run cy:open -- --config baseUrl=<ADDRESS OF THE APPLICATION>
```

For a headless execution, run:

```bash
npm run cy:run -- --config baseUrl=<ADDRESS OF THE APPLICATION>
```

You can use the deployed application for the test, or the local environment. Ideally the results would be the same on both cases.

## Automated test results

After the tests are executed, it will generate a few artefacts as proof of execution, such as screenshots and videos. This is good for reference, in case of an error, or to validate a scenario with the rest of the team.

When a feature file has a instruction without the corresponding implementation step, it will make the automated test fail. **DON'T PANIC**. This is a expected behavior if you're adding a new instruction. One of the developers will be notified when new things are created so they can deal with the implementation of that specific step.

## Data tables

Data tables makes the form filling process way easier and faster. It allows you to insert multiple data at once, without the need to write each step individually. The data table should be inserted after the `I fill the form as follows` instruction, and it should have 3 columns, the first one being the field name, the second one the data to be inserted and the third one the kind of field.

The kind of field is a special instruction that will be used to identify the type of field that is being filled. The possible values are `text`, `select`, `multiselect`, `autocomplete` and `textbox`.

Here's an example of a data table:

```gherkin
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
```

Pay attention to the way the data table is structured, as it is very sensitive to spacing and the number of columns. The `Type` column should be written in lowercase, the `Field name` should be the exact name of the field in the form and the `Value` should be the data to be inserted or selected. The table itself is composed by the `|` character to separate the columns and the rows, it is required to have a `|` at the beginning and at the end of each row. Also pay attention to the spacing required to make the table work, as this follows the Gherkin language format.

One important case here to note is that for the `address` and `contact` information groups, the `contact` or `address` should be created/enabled first. If you declare a `contact` information before the `contact` group is created, the test will fail. The same applies for the `address` group.

For `address` and `contact` we have a special instruction that can reduce the amount of steps required to start the new group.

## Credentials

To avoid credentials being leaked everywhere, we have a special instruction that will be used to login using some specific user types. This instruction uses the `annotation` and it should be used at the `scenario` level. The annotation should be written in the following format:

```gherkin
@loginAsEditor
Scenario: Submit individuals
  When I click on the "Create client" button
  And I can read "Create client"
```

The `@loginAsEditor` is a special instruction that will be used to login as a specific user type. The `@` symbol is required to be used before the instruction, and it should be placed at the beginning of the scenario. The instruction itself should be written in camelCase, with the first letter of each word in uppercase. The instruction should be written right above the `Scenario` keyword. The possible values for it can be found down below. Keep in mind that it will only login and it will stop right after the login is done, on the expected page.

Here's a list of possible instructions that can be used:

| Instruction      | Description                           |
| ---------------- | --------------------------------------|
| @loginAsEditor   | Login as a staff editor user type     |
| @loginAsViewer   | Login as a staff viewer user type     |
| @loginAsAdmin    | Login as a staff admin user type      |
| @loginAsBCeID    | Login as a BCeID user type            |
| @loginAsBCSC     | Login as a BC Services Card user type |
