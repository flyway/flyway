--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--
-- Must
-- be
-- exactly
-- 13 lines
-- to match
-- community
-- edition
-- license
-- length.
--

CREATE TABLE `flyway_test` (
  `ID` int(11) NOT NULL,
  `NAME` text COLLATE latin1_general_cs,
  `VAL` text COLLATE latin1_general_cs,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_cs;

LOCK TABLES `flyway_test` WRITE;
/*!40000 ALTER TABLE `flyway_test` DISABLE KEYS */;
INSERT INTO `flyway_test` VALUES (1,'Year','YEAR({value})'),(2,'Year, Quarter','CONCAT(YEAR({value}), CONCAT(\'-\', QUARTER({value})))'),(3,'Year, Month','CONCAT(YEAR({value}), CONCAT(\'-\', MONTH({value})))'),(4,'Year, Week','CONCAT(YEAR({value}), CONCAT(\'-\', WEEK({value})))');
/*!40000 ALTER TABLE `flyway_test` ENABLE KEYS */;
UNLOCK TABLES;