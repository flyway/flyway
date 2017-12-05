--
-- Copyright 2010-2017 Boxfuse GmbH
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

DROP FUNCTION IF EXISTS format_time;

DELIMITER ||

CREATE FUNCTION format_time( picoseconds BIGINT UNSIGNED )
RETURNS varchar(16) CHARSET utf8
   DETERMINISTIC
BEGIN

  IF picoseconds IS NULL THEN

    RETURN NULL;

  ELSE

    RETURN CAST(CONCAT(ROUND(picoseconds / 1000000000000, 2)) AS DECIMAL(10,6));

  END IF;

END;
||

DELIMITER ;

select format_time ( NULL );