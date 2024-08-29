---
subtitle: Check
---

# Check

## Overview

In Flyway, "Checks" is the collective term we use for the pre- or post-deployment analysis of some aspect of your database migration. Checks are instantiated using the top level `check` command.

Before performing a deployment to the target database (most notably, production), you might want to look over what you're about to do and understand one or more of the following:
### [Changes](<Concepts/Check Changes Concept>)
- Does this set of changes affect the objects I expect it to, or will I be inadvertently having an impact on something else?

### [Drift](<Concepts/Check Drift Concept>)
- What database changes have been made recently, that coincide with the changes in database performance we are seeing? Are the two related?
- Is the production database in the same state you were expecting when I began developing my changes? 
- Has anything about the target database changed that would mean my changes no longer have the desired effect?

### [Code](<Concepts/Check Code Concept>)
- Does our approach to database change development meet our internal policies? 
- Are our migration scripts adhering to our naming conventions, for example? 
- Are we following the security best-practices required of us by our external regulatory requirements?








