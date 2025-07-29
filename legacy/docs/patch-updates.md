# Patch Updates in Forest Client

This document explains the patch update mechanism in the Forest Client application, detailing the involved files, their responsibilities, and the order of operations based on the source code.

## Overview
Patch updates are managed through a set of services implementing the `ClientPatchOperation` interface. Each service is responsible for handling patch operations for a specific section of the client data, such as client details, contact information, addresses, and more. The patching process is orchestrated by `ClientPatchService`, which delegates patch requests to the appropriate services in a defined order.

## Key Files and Their Roles

### 1. `ClientPatchOperation.java`
- **Role:** Interface defining the contract for patch operations. Each implementation specifies a prefix (section of client data), restricted paths, and the logic to apply a patch.
- **Key Methods:**
  - `getPrefix()`: Identifies the section handled.
  - `getRestrictedPaths()`: Lists fields that cannot be patched.
  - `applyPatch(...)`: Applies the patch logic.

### 2. `ClientPatchService.java`
- **Role:** Central service that receives patch requests and delegates them to all registered `ClientPatchOperation` implementations.
- **Order:** Uses a list of services (`partialServices`) and applies patches in their defined order using reactive chaining.

### 3. Patch Operation Services
Each service implements `ClientPatchOperation` and is annotated with `@Order` to define execution sequence. Below is the order and purpose:

#### Order 1: `PatchOperationClientService.java`
- **Handles:** Core client fields (e.g., wcbFirmNumber, clientComment).
- **Purpose:** Applies patches to main client entity, filtering out restricted fields.

#### Order 2: `PatchOperationNameService.java`
- **Handles:** Client name fields.
- **Purpose:** Updates client name and related fields, also manages reason codes for name changes.

#### Order 3: `PatchOperationIdService.java`
- **Handles:** Client identification fields.
- **Purpose:** Manages updates to client ID fields and their associated reason codes.

#### Order 4: `PatchOperationStatusService.java`
- **Handles:** Client status code.
- **Purpose:** Updates client status and tracks reason for status changes.

#### Order 5: `PatchOperationLocationService.java`
- **Handles:** Client location details (addresses, phones, etc.).
- **Purpose:** Applies add/replace operations to client location entities, including address fields.

#### Order 6: `PatchOperationAddressService.java`
- **Handles:** Address reason codes.
- **Purpose:** Updates reason codes for address changes in audit tables.

#### Order 7: `PatchOperationContactRemoveService.java`
- **Handles:** Removal of contact entities.
- **Purpose:** Deletes contact records based on patch instructions.

#### Order 8: `PatchOperationContactEditService.java`
- **Handles:** Editing contact details.
- **Purpose:** Updates contact fields such as name, type, phone, and email.

#### Order 9: `PatchOperationContactAddService.java`
- **Handles:** Adding new contacts.
- **Purpose:** Inserts new contact records, generating IDs and populating all required fields.

#### Order 10: `PatchOperationContactAssociationService.java`
- **Handles:** Contact-location associations.
- **Purpose:** Manages add/remove/replace operations for associating contacts with locations.

#### Order 11: `PatchOperationDoingBusinessService.java`
- **Handles:** "Doing Business As" names.
- **Purpose:** Updates or creates DBA records for the client.

#### Order 12: `PatchOperationsRelatedClientService.java`
- **Handles:** Related client information.
- **Purpose:** Manages patch operations for related clients, including adding, replacing, and removing relationships.

## Order of Operation
The patching process follows the order defined by the `@Order` annotation on each service. This ensures that updates are applied in a logical sequence, respecting dependencies and data integrity:

1. **Client core fields**
2. **Name fields**
3. **Identification fields**
4. **Status fields**
5. **Location/address fields**
6. **Address reason codes**
7. **Contact removal**
8. **Contact editing**
9. **Contact addition**
10. **Contact associations**
11. **Doing Business As names**
12. **Related client information**

## Reason Behind Each Operation
- **Separation of Concerns:** Each service targets a specific section of the client data, making the patch logic modular and maintainable.
- **Restricted Paths:** Critical fields are protected from modification to ensure data integrity and compliance.
- **Reason Codes:** For sensitive changes (e.g., name, ID, status), reason codes are tracked for audit and business rules.
- **Order Enforcement:** The use of `@Order` ensures that operations are performed in a sequence that avoids conflicts and maintains consistency.

## Summary
Patch updates in the Forest Client are managed through a well-structured, ordered set of services. Each service is responsible for a distinct part of the client data, and the overall process is coordinated by `ClientPatchService`. This design provides flexibility, auditability, and robustness for client data updates.
