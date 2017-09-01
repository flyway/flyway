/*
 * Copyright 2017 ScuteraTech Unip.LDA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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