--
-- Copyright 2010-2018 Boxfuse GmbH
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

CREATE TABLE po_xml_tab(
  poid number,
  poDoc XMLTYPE);

CREATE TABLE po_xtab of XMLType;

CREATE TABLE test (
id number(10) not null,
xmlDocument xmltype,
primary key (id)
);

CREATE TABLE test2 (
revid number(10) not null,
test_id number(10) not null,
xmlDocument xmltype,
primary key (revid,test_id),
foreign key (test_id) references test(id)
);

CREATE INDEX test_xmlindex_ix ON test(xmlDocument) indextype IS xdb.xmlindex
PARAMETERS ('PATH TABLE test_path_table
PATH ID INDEX test_path_id_ix
ORDER KEY INDEX test_order_key_ix
VALUE INDEX test_value_ix');

CREATE INDEX test2_xmlindex_ix ON test2(xmlDocument) indextype IS xdb.xmlindex
PARAMETERS ('PATH TABLE test2_path_table
PATH ID INDEX test2_rev_path_id_ix
ORDER KEY INDEX test2_order_key_ix
VALUE INDEX test2_value_ix');
