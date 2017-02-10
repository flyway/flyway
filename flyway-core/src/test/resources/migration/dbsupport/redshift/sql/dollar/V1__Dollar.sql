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

CREATE TABLE dollar (str VARCHAR(100));

INSERT INTO dollar VALUES($$Hello 'quotes']$$);
INSERT INTO dollar VALUES($abc$Hello 'quotes' and $'s$abc$);
INSERT INTO dollar VALUES($$Hello ''quotes'$$);
INSERT INTO dollar VALUES($$Hello $quotes$ $$);
INSERT INTO dollar VALUES($abc$Hello $$quotes$$ $abc$);
