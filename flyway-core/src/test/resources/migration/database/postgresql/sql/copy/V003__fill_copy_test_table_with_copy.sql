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

