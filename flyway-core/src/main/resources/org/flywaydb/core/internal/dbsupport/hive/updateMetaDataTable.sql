-- Add new metadata row
INSERT INTO TABLE ${schema}.${table}
SELECT * FROM
(SELECT
    '${version_val}' version,
    ${installed_rank_val} installed_rank,
    '${description_val}' description,
    '${type_val}' type,
    '${script_val}' script,
    ${checksum_val} checksum,
    '${installed_by_val}' installed_by,
    CURRENT_TIMESTAMP() installed_on,
    ${execution_time_val} execution_time,
    ${success_val} success
) schemav;