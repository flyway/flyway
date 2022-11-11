---
layout: documentation
menu: knownparserlimitations
subtitle: Known Parser Limitations
---

## Known Parser Limitations

We frequently get bugs about the Flyway parser being unable to handle certain sql scripts.

This page was created to document some of the known issues, weirdnesses and workarounds.

If none of the workarounds work for you, or if you think the workaround is unsuitable for any reason, then please create a GitHub issue with reproduction steps, including the contents of the file, or attaching the file itself if possible.

### Control Flow Keyword Handling

If you see any of the error messages `Delimiter changed inside statement`, `Incomplete statement`, or `Unable to decrease block depth below 0` it may be because Flyway's control flow handling encountered an error. This can be caused by a number of different reasons:

- It may be because the particular keyword is not yet supported (in which case please create a Github issue).
- It may be because you haven't closed that block (for example, and `IF` not closed by an `END IF`).
- It may be because you are using a control flow keyword as a variable name (which Flyway does not support). Control flow keywords are most commonly `BEGIN` and `END`, but may also include `IF`, `CASE`, `REPEAT`, `WHILE`, and more depending on database type. In this case please change the variable name to one that is not used for control flow.

### Multiline Comments

We often encounter nested multiline comments, such as `/* ... /* ... */ ... */`.
Flyway requires that all multiline comments are closed, so `/* ... /* ... */` would be invalid.
