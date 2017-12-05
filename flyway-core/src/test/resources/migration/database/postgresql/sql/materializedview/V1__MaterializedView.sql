--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE MATERIALIZED VIEW v_dwh_dim_date_mat AS select now();

CREATE MATERIALIZED VIEW """v_dwh_dim_date_mat2""" AS select now();

CREATE OR REPLACE VIEW v_dwh_dim_date AS SELECT * from v_dwh_dim_date_mat;