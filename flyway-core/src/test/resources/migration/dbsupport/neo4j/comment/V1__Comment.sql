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

/**
 * @author Ricardo Silva (ScuteraTech)
 *
 */

//First ' comment'
 
CREATE (t:test_user
  //' second '
  { name:"Mr. Iße T"});

CREATE (t:test_user

//  'third '
 
  { name:"Mr. Semicolon"});

// 'fourth'
CREATE (t:test_user
// ' fifth'
 { name:"Mr. Semicolon2"});

CREATE (
//  'sixth'
   t:test_user { name:"Mr. Iße T2"});
