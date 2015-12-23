--
-- Copyright 2010-2015 Axel Fontaine
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

-- Update version ranks if needed
UPSERT INTO "${schema}"."${table}"
SELECT     "version",
           "version_rank" + 1 AS "version_rank",
           "installed_rank"
           "version",
           "description",
           "type",
           "script",
           "checksum",
           "installed_by",
           CURRENT_TIME(),
           "execution_time",
           "success"
FROM       "${schema}"."${table}"
WHERE      "version_rank" >= ${version_rank_val};

-- Add new metadata row
UPSERT INTO "${schema}"."${table}" VALUES (
    '${version_val}',
    ${version_rank_val},
    ${installed_rank_val},
    '${description_val}',
    '${type_val}',
    '${script_val}',
    ${checksum_val},
    '${installed_by_val}',
    CURRENT_TIME(),
    ${execution_time_val},
    ${success_val}
);