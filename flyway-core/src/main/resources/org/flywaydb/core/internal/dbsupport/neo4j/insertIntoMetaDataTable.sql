MATCH (sv :schema_version) 
CREATE (sv)-[:schema_versionToMigration]->
(Migration :Migration 
	{installed_rank: ${installed_rank} , 
	version : ${version_val},
	description:${description_val},
	type:${type},
	script:${script},
	checksum:${checksum},
	installed_by:${installed_by},
	installed_on:${installed_on},
	execution_time:${execution_time},
	success:${success}});