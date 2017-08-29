MERGE (t:all_misters); 

MATCH (t:all_misters) 
OPTIONAL MATCH (u : test_user) 
WHERE u.name STARTS WITH "Mr." 
CREATE (t)-[r:nameStartsMr]->(u); 