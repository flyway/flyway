---
subtitle: redgateCompare.oracle.data.options.comparison
---

{% include enterprise.html %}

This namespace contains the configurations relating to Oracle static data comparison.

## Settings

| Setting                                                                                                                                                                                             | Type    | Description                                                                                                      |
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|------------------------------------------------------------------------------------------------------------------|
| [`checkTablesForData`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Data Comparison Options Namespace/Oracle Data Check Tables For Data Setting>)               | Boolean | Check each table for data.                                                                                       |
| [`includeViews`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Data Comparison Options Namespace/Oracle Data Include Views Setting>)                             | Boolean | Include views in the comparison.                                                                                 |
| [`trimTrailingSpaces`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Data Comparison Options Namespace/Oracle Data Trim Trailing Spaces Setting>)                | Boolean | If the data in two columns differs only by the number of spaces at the end of the string, treat it as identical. |
| [`ignoreControlCharacters`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Data Comparison Options Namespace/Oracle Data Ignore Control Characters Setting>)      | Boolean | Ignore control character differences in text data.                                                               |
| [`ignoreWhiteSpace`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Data Comparison Options Namespace/Oracle Data Ignore White Space Setting>)                    | Boolean | Ignore whitespace differences in text data.                                                                      |
| [`includeSourceTables`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Data Comparison Options Namespace/Oracle Data Include Source Tables Setting>)              | Boolean | Allow data comparison when tables exist in source but not in target.                                             |
| [`ignoreDateTypeDifferences`](<Configuration/Redgate Compare Namespace/Redgate Compare Oracle Namespace/Oracle Data Comparison Options Namespace/Oracle Data Ignore Date Type Differences Setting>) | Boolean | Ignore data differences in date type columns.                                                                    |