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
