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

--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- Data for Name: copy_test; Type: TABLE DATA; Schema: public; Owner: arnd
--

COPY copy_test (c1, c2, c3) FROM stdin;
1	utf8: ümlaute: äüß	NaN
2	\N	123
3	text	123.234444444444449
\.

COPY copy_test (c1, c2, c3)
  FROM stdin;
4	utf8: ümlaute: äüß	NaN
5	\N	123
6	text	123.234444444444449
\.


--
-- Name: copy_test_c1_seq; Type: SEQUENCE SET; Schema: public; Owner: arnd
--

SELECT pg_catalog.setval('copy_test_c1_seq', 3, true);


--
-- PostgreSQL database dump complete
--

