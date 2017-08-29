MERGE (t:other_view);

MATCH (u : test_user)
WHERE u.name STARTS WITH "Mr."
OPTIONAL MATCH (t:other_view)
CREATE (t)-[r: nameStartsMr]-> (u);