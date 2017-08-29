MERGE (c:couple1);

MATCH (u : test_user1)
WHERE u.name STARTS WITH "Mr."
OPTIONAL MATCH (c:couple1)
CREATE (v)-[r: coupleWith]-> (u);

MERGE (c:couple2);

MATCH (u : test_user2)
WHERE u.name STARTS WITH "Mr."
OPTIONAL MATCH (c:couple2)
CREATE (v)-[r: coupleWith]-> (u);

MERGE (c:couple3);

MATCH (u : test_user3)
WHERE u.name STARTS WITH "Mr."
OPTIONAL MATCH (c:couple3)
CREATE (v)-[r: coupleWith]-> (u);