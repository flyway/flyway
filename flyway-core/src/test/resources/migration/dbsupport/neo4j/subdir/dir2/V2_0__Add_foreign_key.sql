CREATE (c:couple {name:"T_Semicolon"});

MATCH (:all_misters)-[]->(u : test_user)
OPTIONAL MATCH (c:couple)
CREATE (c)-[r: areCouple]-> (u);