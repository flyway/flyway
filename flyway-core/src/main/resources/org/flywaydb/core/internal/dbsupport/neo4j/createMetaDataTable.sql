MERGE (schemaVersion :SchemaVersion);

MATCH (sv :SchemaVersion) 
CREATE (sv)-[:SchemaVersionToMigration]->
(Migration? :Migration 
	{installed_rank: 'installed_rank' , 
	version : 'version',
	description:'description',
	type:'type',
	script:'script',
	checksum:'checksum',
	installed_by:'installed_by',
	installed_on:'installed_on',
	execution_time:'execution_time',
	success:'success'});