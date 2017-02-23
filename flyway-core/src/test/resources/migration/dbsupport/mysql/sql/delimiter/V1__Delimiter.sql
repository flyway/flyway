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

delimiter ;

select 1;

select 2;

delimiter $$

select 3;
$$

select 4;
$$

delimiter #

create procedure init_fact_references()
  begin
    start transaction;
    alter table facts add reference int;
    update facts set reference = (position + 1) where publication_date is not null;
    update facts set reference = 0 where publication_date is null;
    commit;
  end #

delimiter ;

select 5;