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

BEGIN
  IF USER != SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') THEN
    RAISE_APPLICATION_ERROR(-20000, 'Locator/Spatial indexes can be created in user''s own schema only!');
  END IF;
END;
/

CREATE TABLE GEO_TEST (
  GEO MDSYS.SDO_GEOMETRY
);

INSERT INTO USER_SDO_GEOM_METADATA (TABLE_NAME, COLUMN_NAME, DIMINFO, SRID)
  VALUES ('GEO_TEST', 'GEO',
    MDSYS.SDO_DIM_ARRAY
      (MDSYS.SDO_DIM_ELEMENT('LONG', -180.0, 180.0, 0.05),
       MDSYS.SDO_DIM_ELEMENT('LAT', -90.0, 90.0, 0.05)
      ),
     8307);

CREATE INDEX GEO_TEST_GEO_IDX ON GEO_TEST(GEO) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS('sdo_indx_dims=2, layer_gtype=point');
