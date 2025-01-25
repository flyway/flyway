---
subtitle: Schema mapping
---

# Schema mapping

For any flyway command which performs a database comparison under the hood ([`diff`](<Commands/Diff>), [
`prepare`](<Commands/Prepare>)), the following rules apply in relation to mapping the source schemas to the target
schemas:

* For SQL Server databases, database comparison will compare objects in all schemas, irrespective of any schema
  configuration. Schemas can be suppressed from the comparison using filters.
* For other databases, only the schemas specified will be compared.
    * If no explicit schemas are set for the comparison source, then the source schemas will be assumed to be the same
      as those specified for the target
    * If no explicit schemas are set for the comparison target, then the target schemas will be assumed to be the same
      as those specified for the source
    * If explicit schema model schemas are set for source and target, then source schemas will be automatically mapped
      to target schemas in the same order as they are specified

> [!Important]  
> When the schemas are different between source and target, make sure to configure schemas for both sides. When either
> source or target is a schema model, the schema model schemas can be specified using
> [`schemaModelSchemas`](<Configuration/Parameters/Flyway/Schema Model Schemas>).