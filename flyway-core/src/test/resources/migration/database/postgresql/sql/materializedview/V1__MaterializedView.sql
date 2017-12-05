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

CREATE MATERIALIZED VIEW v_dwh_dim_date_mat AS select now();

CREATE MATERIALIZED VIEW """v_dwh_dim_date_mat2""" AS select now();

CREATE OR REPLACE VIEW v_dwh_dim_date AS SELECT * from v_dwh_dim_date_mat;