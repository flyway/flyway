---
subtitle: redgateCompare.sqlserver.data.options.comparison
---

{% include enterprise.html %}

This namespace contains the configurations relating to SQL Server static data comparison.

## Settings

| Setting                                                                                                                                                                                                    | Type    | Description                                                                                                                    |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|--------------------------------------------------------------------------------------------------------------------------------|
| [`compressTemporaryFiles`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Comparison Options Namespace/SQL Server Data Compress Temporary Files Setting>)   | Boolean | Compress the temporary files that are generated while performing the comparison.                                               |
| [`forceBinaryCollation`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Comparison Options Namespace/SQL Server Data Force Binary Collation Setting>)       | Boolean | For all string data types, forces binary collation irrespective of column collation, resulting in a case-sensitive comparison. |
| [`treatEmptyStringAsNull`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Comparison Options Namespace/SQL Server Data Treat Empty String As Null Setting>) | Boolean | When this option is selected, empty strings (no characters) will be treated as `NULL`.                                         |
| [`trimTrailingWhiteSpace`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Comparison Options Namespace/SQL Server Data Trim Trailing White Space Setting>)  | Boolean | If the data in two columns differs only by whitespace at the end of the string, the data will be considered to be identical.   |
| [`useChecksumComparison`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Comparison Options Namespace/SQL Server Data Use Checksum Comparison Setting>)     | Boolean | Performs a checksum on each table prior to comparison.                                                                         |
| [`useMaxPrecisionForFloatComparison`](<Configuration/Redgate Compare Namespace/Redgate Compare SQL Server Namespace/SQL Server Data Comparison Options Namespace/>)                                        | Boolean | Compare floating point values to the maximum 17 digits of precision.                                                           |