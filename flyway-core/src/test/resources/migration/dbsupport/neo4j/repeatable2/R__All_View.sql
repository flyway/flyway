MERGE (t:all_view);

MATCH (u : test_user)
WHERE u.name STARTS WITH "Mr."
OPTIONAL MATCH (t:all_view)
CREATE (t)-[r: nameStartsMr]-> (u);