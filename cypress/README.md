# Automated End-to-End User journey tests with Cypress

This repository deals with automated user journey tests. We use cypress for automation and Cucumber and Gherking as the framework to describe the scenarios.
The idea behind this is to allow a colaborative environment where anyone can write down a set operations that composes a Journey to be tested, and the developers will make sure to implement it.

The main objective is to cover as many cases as possible by allowing non developers to describe scenarios that sometimes are forgotten by the development team and allow different point of view for journeys.

The below sections are intended to explain how the framework works, along with a few instructions. Please make sure to read it carefuly and don't be afraid to play around.

## Introduction to Writing Cucumber/Gherkin Files for End-to-End Tests

[Cucumber](https://cucumber.io/) is a popular tool used for behavior-driven development (BDD) in software testing. It allows you to write executable specifications in a natural language format called Gherkin. Gherkin is a plain-text language that is easy to understand and can be used by non-technical stakeholders as well. In this tutorial, we will guide you through the basics of writing a Cucumber/Gherkin file for end-to-end tests. To learn more about Cucumber, Gherking and BDD, you can enroll on a [free course](https://school.cucumber.io/courses/bdd-overview-for-business-analysts-and-product-owners).

It is always good to read about Gherkin language format before start but in summary, Gherkin is a writing format that leverages plain english to allow users to describe the expected behavior in simple steps that is simple to understand by non-technical persons, but also makes sense to be executed in that specific order. Think of it like a set of steps in a cake recipe. Each test is written in a file with the `.feature` extension and is saved inside [cypress/e2e](cypress/e2e) folder on this repo, you can check some of the existing files for reference. 

    Avoid using names with spaces or special characters. Replace spaces with `_` and special characters for it's non-special character counterpart

Here is how to get started:

### Step 1: Creating a Feature File

Create a new file with the `.feature` extension inside the [cypress/e2e](cypress/e2e) folder. This file will contain your Gherkin scenarios. Start by writing a short description of the feature you are testing, preceded by the Feature keyword. For example:

```gherkin
Feature: User Registration
  As a new user
  I want to register on the website
  So that I can access exclusive content
```

### Step 2: Writing Scenarios

Scenarios represent specific test cases. Each scenario consists of a series of steps. Steps can be one of the following: **Given**, **When**, **Then**, **And**, or **But**. Here's an example scenario for our user registration feature:

```gherkin
Scenario: Successful user registration
  Given I am on the registration page
  When I fill in the registration form with valid information
  And I click the "Register" button
  Then I should see a success message
```

Congratulations! You have successfully written a basic Cucumber/Gherkin file for end-to-end tests. You can continue adding more scenarios and step definitions to cover different test cases in the application.

Remember, the power of Cucumber lies in its ability to bridge the communication gap between technical and non-technical team members, enabling collaboration and providing a common language for defining software behavior.


## For Developers

The developer will implement one `.ts` file for each `.feature` file, using the same name. Ex: `sample.feature` will have a corresponding `sample.ts` file.

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

When a feature file exists without the corresponding implementation, it will make the automated test fail. **DON'T PANIC**. This is the expected behavior if you're adding a new scenario. One of the developers will be notified when new things are created so they can deal with the implementation of that scenario.