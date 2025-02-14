---
subtitle: Oracle Post Filter Format
---

## Format

Oracle post filter files have the .scpf extension.
Under the hood they are structured as XML.

Example:
```xml
<?xml version="1.0" encoding="utf-16" standalone="yes"?>
<Filter version="1" type="Filter">
  <exclude type="ExcludeList" version="2" />
  <excludeContaining>TABLE_ONE</excludeContaining>
  <includeContaining />
</Filter>
```
