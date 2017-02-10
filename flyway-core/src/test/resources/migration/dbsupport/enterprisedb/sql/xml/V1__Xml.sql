--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
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
