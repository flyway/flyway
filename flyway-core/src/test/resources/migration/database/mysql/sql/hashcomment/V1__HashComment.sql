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

delimiter $$

#
# Inserts task references into the tree_views table to all results from the base query that match the given filter criteria.
# Base query must (at least) return the columns "id" and "parent_id".
#
create procedure tv_$copy_filtered (
        p_msg text
)
begin
    call log('   done');
end;
$$
