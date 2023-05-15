# Mail Format for Notifications

This document describes the available variable names to be used inside the mail templates along with the format for mail templates.

## Mail Template Format

The mail template is just an **HTML** file with some variables that will be replaced with the values on the object being used.
Each user flow will have its own set of variables, and this is due to the amount of context involved on each flow.
The template engine used for this is called [FreeMarker](https://freemarker.apache.org/) and anything that the engine supports can be used.

If you encounter unexpected behaviour while using the template engine in our application, we encourage you to open an [issue on GitHub](#opening-an-issue-on-github).
This will allow us to investigate and resolve the issue as quickly as possible.

## File Location

Template files are located inside the [templates folder](src/main/resources/templates) located at `src/main/resources`. You can easily navigate to it and edit the existing files, or create a new one.

## Available variables

As the FreeMarker template engine describes, you can set a few variables to be used to replace parts of the template when needed. These variables are formed using a set of special characters.
The template engine replaces `${...}` with the content of the variable. The variable name is what is contained inside those special characters, such as `${name}` for name and so on.

The variables we are exposing follow the . (dot) notation format to navigate through it. This means that you can have objects with depth, meaning that you can have variables with similar names (such as name for example)
being used by multiple entities, as each one will have a unique *depth* to it.

This also makes it easy to identify the context of the consumed data without acronyms.

    Example:
    
    To get the name of the user, a variable could be represented as `${user.name}`, 
    and this will not clash with the name of the company if the variable for it is `${company.name}`.

### Submission Approval

We have **3** main properties that contain sub-properties. Some of the properties are lists of other properties, and when this happens,
we will have a zero-based index value for that property, usually with the index wrapped around `[` and `]` like `[2]` for example for the **third** entry on that list.

The file name for this is [registration.html](src/main/resources/templates/registration.html) and can be found inside the [src/main/resources/templates/](src/main/resources/templates/) folder.

#### Addresses

Information related to the location of this client, with an index-based entry for each location.
The top-level property name is `address` and for each entry, you have to pass the index of the location,
wrapped around `[` and `]`. Each location will have the following properties:

- name
- address
- city
- country
- postalCode
- province
- contact (see below)

So for example, to get the city of the **first** location, the variable would be `${address.[0].city}`.

##### Contacts

Information related to contacts for this client, with an index-based entry for each contact.
The top-level property name is `contact` and for each entry, you have to pass the index of the contact,
wrapped around `[` and `]`. Each contact will have the following properties:

- name (will be the junction of first and last name)
- firstName
- lastName
- email
- phone

So for example, to get the name of the **first** contact, the variable would be `${contact.[0].name}`.

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


### 100% Match when Searching

When doing an auto-complete search for a client name, one thing that can happen is what we call **100% match**. This happens when both the name and the incorporation number
matches with one entry already present inside the Oracle database. When this happens, we will automatically email the user with some information regarding the existing entry.

For that, we have a single entry with the following parameters:

- number (Client number on Oracle)
- name (The legal name of the client)
- status (Actual status on Oracle)
- type (Type of the client on Oracle)
- identifier (The incorporation number for this client on Oracle)


The file name for this is [matched.html](src/main/resources/templates/matched.html) and can be found inside the [src/main/resources/templates/](src/main/resources/templates/) folder.

## Opening an Issue on GitHub

To open an issue, please follow these steps:

1. Go to the [Issues Tab](issues)
2. Click on the green "New issue" button to start a new issue.
3. Provide a descriptive title and a detailed description of the issue you are experiencing.
4. Include any relevant error messages or screenshots that may help us understand the issue.
5. Add any labels or assignees that may be relevant to the issue.
6. Click "Submit new issue" to create the issue.

Once the issue is created, our team will review it and respond as soon as possible. 
