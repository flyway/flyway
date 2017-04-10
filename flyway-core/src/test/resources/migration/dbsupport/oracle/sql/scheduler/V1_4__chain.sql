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

declare
  l_prefix VARCHAR2(131) := '"' || SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') || '".';
begin
  dbms_scheduler.create_chain(
    chain_name => l_prefix || 'TEST_CHAIN');
  dbms_scheduler.define_chain_step(
    chain_name   => l_prefix || 'TEST_CHAIN',
    step_name    => 'TEST_STEP_1',
    program_name => l_prefix || 'TEST_PROGRAM');
end;
/

