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

-- this package generates types
CREATE OR REPLACE PACKAGE PKG_PIPE_RECORD AS

TYPE t_rec IS RECORD (
    field1 NUMBER,
    field2 VARCHAR2(100)
);

TYPE t_rec_coll IS TABLE OF t_rec;

FUNCTION func RETURN t_rec_coll PIPELINED;

END;
/