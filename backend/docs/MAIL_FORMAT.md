# Mail Format for Notifications

This document describes the available variable names to be used inside the mail templates along with the format for mail templates.

## Mail Template Format

The mail template is just an **HTML** file with some variables that will be replaced with the values on the object being used. 
Each user flow will have its own set of variables, and this is due to the amount of context involved on each flow. 
The template engine used for this is called [FreeMarker](https://freemarker.apache.org/) and anything that the engine supports can be used.

If you encounter unexpected behavior while using the template engine in our application, we encourage you to open an [issue on GitHub](#opening-an-issue-on-github). 
This will allow us to investigate and resolve the issue as quickly as possible.

## File Location

Template files are located inside the [templates folder](src/main/resources/templates) located at `src/main/resources`. You can easily navigate to it and edit the existing files, or create a new one.

## Available variables

As the FreeMarker template engine describes, you can set a few variables to be used to replace parts of the template when needed. These variables are formed using a set of special characters. 
The template engine replaces `${...}` with the content of the variable. The variable name is what is contained inside those special characters, such as `${name}` for name and so on. 

The variables we are exposing follow the . (dot) notation format to navigate through it. This means that you can have objects with depth, meaning that you can have variables with similar names (such as name for example)
being used by multiple entities, as each one will have a unique *depth* to it.

This also makes it easy to identify the context of the data being consumed without the need of acronyms. 

    Example:
    
    To get the name of the user, a variable could be represented as `${user.name}`, 
    and this will not clash with the name of the company, if the variable for it is `${company.name}`.


### Submission Approval

We have **3** main properties that contains sub-properties. Some of the properties are list of other properties, and when this happens,
we will have a zero-based index value for that property, usually with the index wrapped around `[` and `]` like `[2]` for example for the **third** entry on that list. 

#### Location

Information related to the location for this client, with an index based entry for each location. 
The top level property name is `location` and for each entry you have to pass the index of the location, 
wrapped around `[` and `]`. Each location will have the following properties:

- address
- city
- country
- postalCode
- province
- contact (see below)

So for example, to get the city of the **first** location, the variable would be `${location.[0].city}`.

##### Contacts

Information related to contacts is present inside the location data. Each location can have multiple contacts, 
so it needs to follow the index based format, where for each entry you have to pass the index of the contact,
wrapped around `[` and `]`. Each contact will have the following properties:

- name (will be the junction of first and last name)
- firstName
- lastName
- email
- phone

So for example, to get the name of the **first** contact on the **second** location, 
the variable would be `${location.[1].contact.[0].name}`.

#### Business

Information related to the client itself, with properties related to the information we acquired from BC Registry 
or in case of clients that are not registered on BC Registry itself, the information provided by the user. 
The properties on business are:

- legalType
- name
- goodStanding
- incorporation
- businessType
- clientType
  
So for example, to get the name of the business, the variable would be `${business.name}`.

#### Submitter

Information related to the person who submitted the data. 
Some of those fields will be automatically extracted from BCeID and the authentication system

- email
- name (will be the junction of first and last name)
- phone
- lastName
- firstName
- id

So for example, to get the name of the business, the variable would be `${submitter.name}`.


## Opening an Issue on GitHub 

To open an issue, please follow these steps:

1. Go to the [Issues Tab](issues)
2. Click on the green "New issue" button to start a new issue.
3. Provide a descriptive title and detailed description of the issue you are experiencing.
4. Include any relevant error messages or screenshots that may help us understand the issue.
5. Add any labels or assignees that may be relevant to the issue.
6. Click "Submit new issue" to create the issue.

Once the issue is created, our team will review and respond as soon as possible. 
