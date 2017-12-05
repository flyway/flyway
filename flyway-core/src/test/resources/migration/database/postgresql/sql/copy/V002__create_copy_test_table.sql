--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
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

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: copy_test; Type: TABLE; Schema: public; Owner: arnd; Tablespace: 
--

CREATE TABLE copy_test (
    c1 integer NOT NULL,
    c2 character varying,
    c3 double precision
);

--
-- Name: copy_test_c1_seq; Type: SEQUENCE; Schema: public; Owner: arnd
--

CREATE SEQUENCE copy_test_c1_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: c1; Type: DEFAULT; Schema: public; Owner: arnd
--

ALTER TABLE ONLY copy_test ALTER COLUMN c1 SET DEFAULT nextval('copy_test_c1_seq'::regclass);


--
-- PostgreSQL database dump complete
--

