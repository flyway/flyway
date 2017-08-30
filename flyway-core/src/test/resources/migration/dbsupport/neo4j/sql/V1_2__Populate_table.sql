MERGE (:all_misters); 


MATCH (u : test_user) 
WHERE u.name STARTS WITH "Mr." 
OPTIONAL MATCH (t:all_misters) 
CREATE (t)-[r:nameStartsMr]->(u); 