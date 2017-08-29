  /*
   First ' comment
 */
CREATE (t:test_user
  name VARCHAR(25) NOT NULL,
  -- second '
  { name:"Mr. Iße T"});

CREATE (t:test_user
/*
  third '
 */
  { name:"Mr. Semicolon"});

-- 'fourth'
CREATE (t:test_user
-- ' fifth
 { name:"Mr. Semicolon2"});

CREATE (
/*'
  sixth
 '*/
   t:test_user { name:"Mr. Iße T2"});
