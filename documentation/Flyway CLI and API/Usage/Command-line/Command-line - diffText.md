---
pill: cli_diffText
subtitle: 'Command-line: diffText'
---
# Command-line: diffText

{% include enterprise.html %}

Prints the object level differences for a list of changes specified from the diff artifact computed previously by flyway diff.

<a href="Commands/diffText"><img src="assets/command-diffText.png" alt="diffText"></a>

## Usage

<pre class="console"><span>&gt;</span> flyway diffText -diffText.artifactFilename=./diffArtifact -diffText.changes="id1,id2"</pre>

## Options
- `artifactFilename` -  The file path to the diff artifact file. Defaults to the diff.artifactFilename, %temp%/flyway.artifact.diff or working directory if not configured.
- `changes` - A comma separated list of change ids. If unspecified, all changes will be used. May specify - to read changes from stdin

## Sample output

<pre class="console">&gt; flyway diffText

Flyway {{ site.flywayVersion }} by Redgate

--- Table/Schema.table2
+++ Table/Schema.table2
CREATE TABLE Schema.table2 (
    id int NULL,
    name varchar(10) NULL
);
--- Table/Schema.table3
+++ Table/Schema.table3
CREATE TABLE Schema.table3 (
    id int NULL,
    name varchar(15) NULL
);
--- View/Schema.view1
+++ View/Schema.view1
CREATE VIEW Schema.view1 AS select `Schema`.`table1`.`id` AS `id`,`Schema`.`table1`.`name` AS `name` from `Schema`.`table1`;</pre>

## Sample JSON output

<pre class="console">&gt; flyway diffText -outputType=json
{
  "differences" : [ {
    "id" : "MQuXdkRAEhEyd5TIPzVoCStUucA",
    "differenceType" : "Add",
    "objectType" : "Table",
    "from" : {
      "schema" : "Schema",
      "name" : "table2",
      "definition" : "CREATE TABLE Schema.table2 (\n    id int NULL,\n    name varchar(10) NULL\n);"
    },
    "to" : null
  }, {
    "id" : "exY9fOdORvbrXOYNPoqMmifWlEA",
    "differenceType" : "Add",
    "objectType" : "View",
    "from" : {
      "schema" : "Schema",
      "name" : "view1",
      "definition" : "CREATE VIEW Schema.view1 AS select `Schema`.`table1`.`id` AS `id`,`Schema`.`table1`.`name` AS `name` from `Schema`.`table1`;"
    },
    "to" : null
  }, {
    "id" : "K5GPl1kQtgirPltRFC265Oni42M",
    "differenceType" : "Delete",
    "objectType" : "Table",
    "from" : null,
    "to" : {
      "schema" : "Schema",
      "name" : "table3",
      "definition" : "CREATE TABLE Schema.table3 (\n    id int NULL,\n    name varchar(15) NULL\n);"
    }
  } ]
}</pre>
