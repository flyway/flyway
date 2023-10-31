---
subtitle: What is TOML?
redirect_from: /documentation/whatistoml/
---

# What is TOML?

TOML is the new file format to configure Flyway using configuration files, replacing the old `.conf` format.

For the full documentation on TOML, see [here](https://toml.io/en/)

## How to use TOML

### Structure of TOML files

- Every key/value pair should be on a new line, unless they are in an inline table.
- There is **no** need for semicolons or any other character to end a line.
- There should be spaces around every key pair and the equals sign.
- TOML is case-sensitive, so keys must be written exactly as they are in the documentation.

### Strings
Strings are defined by the key followed by the value in double quotes. 

``` toml
string1 = "Hello World" 
```

### Integers
Integers are defined by the key followed by the number. 

``` toml
integer1 = 42 
```

### Booleans
Booleans are defined by the key followed by true or false. 

``` toml
bool1 = true
bool2 = false 
```

### Arrays
Arrays are defined by the key followed by values in square brackets.

A terminating comma is allowed inside the array.

``` toml
array1 = ["value1", "value2", "value3",] 
```

### Maps
Maps are enclosed in curly brackets and include the key being equal to the value.

Terminating commas are not allowed at the end of maps.

```toml
map1 = { key1 = "value1", key2 = "value2" }
```

### Tables
Tables are the collection of key/value pairs. They have their title in square brackets followed by a list of the key pairs.

Different tables can have the same keys.

``` toml
[table1]
key1 = "value1"
key2 = 5

[table2]
key1 = "value2"
key3 = true
```

### Inline Tables
Inline tables are similar to tables but are contained inside curly braces on a single line. 
They use a comma between elements, and do not allow terminating commas.

The formatting of the key/value pairs is the same as for tables.

``` toml
table1 = { key1 = "value1", key2 = "value2" }
```

Tables can also be inlined inside other tables.

``` toml
table1 = { table2 = { key1 = "value1" }, table3 = { key1 = "value2" } }
```