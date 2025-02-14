---
subtitle: Oracle Ignore Rules Format
---

## Format

Oracle ignore rules files have the .scpf extension.
Under the hood they are structured as JSON.

Example:
```json
{
  "filters": {
    "table": [
      "table_one"
    ]
  }
}
```

## Syntax

If no ignore rules are specified, all objects are included.
For each object type, a rule expression will determine the objects to include based on the object's name. The rule expression can also be set up to exclude objects by using the negation operator.

Two new operators have been added to standard Oracle regular expressions.
* \! -; negation: used when we want to reverse the result of the condition (be sure to include the backslash in both the GUI and command-line)
* \& -; logical AND: used when several conditions are combined within one pattern (be sure to include the backslash in both the GUI and command-line)

Suppose we have a database schema with the following tables:

**T1, T2, T3, PRE_TAB1, PRE_TAB2, PRE_TAB1_PRE, PRE_TAB1_SUB, ONE_TAB1_PRE, ONE_TAB2_SUB**


| Intent                                                                   | Pattern                          | Result (included tables are <span style="color:blue">** BOLD**</span>)                                                                                                                             |
|--------------------------------------------------------------------------|----------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Fetch all objects                                                        | . (or leave the field unchanged) | <span style="color:blue">**T1, T2, T3, PRE_TAB1, PRE_TAB2, PRE_TAB1_PRE, PRE_TAB1_SUB, ONE_TAB1_PRE, ONE_TAB2_SUB**</span>                                                                         |
| Fetch exactly one object with the given name: T2                         | T2 (^T2$ will also work)         | T1, <span style="color:blue">**T2**</span>, T3, PRE_TAB1, PRE_TAB2, PRE_TAB1_PRE, PRE_TAB1_SUB, ONE_TAB1_PRE, ONE_TAB2_SUB                                                                         |
| Fetch objects **_containing_** a specified pattern, e.g. TAB             | TAB1+                            | T1, T2, T3, <span style="color:blue">**PRE_TAB1**</span>, PRE_TAB2, <span style="color:blue">**PRE_TAB1_PRE, PRE_TAB1_SUB, ONE_TAB1_PRE**</span>, ONE_TAB2_SUB                                     |
| Fetch objects that **_start with_** a specified pattern, e.g. PRE        | \\!^PRE                          | T1, T2, T3, <span style="color:blue">**PRE_TAB1, PRE_TAB2, PRE_TAB1_PRE, PRE_TAB1_SUB**</span>, ONE_TAB1_PRE, ONE_TAB2_SUB                                                                         |
| Fetch objects that **_do not start with_** a specified pattern, e.g. PRE | T2 (^T2$ will also work)         | <span style="color:blue">**T1, T2, T3**</span>, PRE_TAB1, PRE_TAB2, PRE_TAB1_PRE, PRE_TAB1_SUB, <span style="color:blue">**ONE_TAB1_PRE, ONE_TAB2_SUB**</span>                                     |
| Fetch objects that **_must end with_** a specified pattern, e.g. PRE     | PRE$                             | T1, T2, T3, PRE_TAB1, PRE_TAB2, <span style="color:blue">**PRE_TAB1_PRE**</span>, PRE_TAB1_SUB, <span style="color:blue">**ONE_TAB1_PRE**</span>, ONE_TAB2_SUB                                     |
| Fetch objects that **_must not end with_** a specified pattern           | \\!PRE$                          | <span style="color:blue">**T1, T2, T3, PRE_TAB1, PRE_TAB2**</span>, PRE_TAB1_PRE, <span style="color:blue">**PRE_TAB1_SUB**</span>, ONE_TAB1_PRE, <span style="color:blue">**ONE_TAB2_SUB**</span> |

### Fetching objects using a combination of patterns

**All listed patterns within a group are treated as a logical OR, but in a single pattern custom marker \& can be used which is translated to logical AND.**


| Intent                                                                                                                                                                                                                                | Pattern                               | Result (included tables are <span style="color:blue">** BOLD**</span>)                                                                                                                             |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Fetch objects starting with PRE, containing TAB AND NOT ending with SUB                                                                                                                                                               | ^PRE+\\&TAB\\&\\!SUB$	                | T1, T2, T3, <span style="color:blue">**PRE_TAB1, PRE_TAB2, PRE_TAB1_PRE**</span>, PRE_TAB1_SUB, ONE_TAB1_PRE, ONE_TAB2_SUB                                                                         |
| Fetch objects containing:<br>* table with name T1<br>* tables with name starting from ONE<br>* tables with name starting from PRE and containing phrase TAB and not ending with SUB<br>e.g. T1 OR ^ONE OR (^PRE AND TAB AND NOT SUB$) | "T1”, "^ONE", "^PRE+\\&TAB\\&\\!SUB$" | <span style="color:blue">**T1**</span>, T2, T3, <span style="color:blue">**PRE_TAB1, PRE_TAB2, PRE_TAB1_PRE**</span>, PRE_TAB1_SUB, <span style="color:blue">**ONE_TAB1_PRE, ONE_TAB2_SUB**</span> |
| Exclude all tables                                                                                                                                                                                                                    | \\!                                   | _All tables are excluded_                                                                                                                                                                          |
