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

CREATE TABLE dollar (str VARCHAR(100));

INSERT INTO dollar VALUES($$Hello 'quotes']$$);
INSERT INTO dollar VALUES($abc$Hello 'quotes' and $'s$abc$);
INSERT INTO dollar VALUES($$Hello ''quotes'$$);
INSERT INTO dollar VALUES($$Hello $quotes$ $$);
INSERT INTO dollar VALUES($abc$Hello $$quotes$$ $abc$);

INSERT INTO dollar VALUES($$Hello '
multi-line
quotes;
'$$);

INSERT INTO dollar VALUES($$Hello
multi-line
quotes;
$$);

INSERT INTO dollar VALUES($abc$Hello ';
multi-line;
quotes;
$abc$);

INSERT INTO dollar VALUES(
$abc$
Hello ;
multi-line;
quotes;
$abc$
);
