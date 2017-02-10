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

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table account
--

DROP TABLE IF EXISTS account;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE account (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  communication_email varchar(255) DEFAULT NULL,
  document_expiration_days int(11) NOT NULL,
  account_key varchar(60) NOT NULL,
  name varchar(255) NOT NULL,
  postal_account varchar(255) NOT NULL,
  postal_cust_file_ref_prefix varchar(255) NOT NULL,
  postal_dep_ref_prefix varchar(255) NOT NULL,
  postal_mail_id_cnt bigint(20) NOT NULL,
  postal_mail_id_max bigint(20) NOT NULL,
  postal_mail_id_min bigint(20) NOT NULL,
  postal_mail_ref_prefix varchar(255) NOT NULL,
  postal_mode varchar(255) NOT NULL,
  postal_priority varchar(255) NOT NULL,
  postal_reg_id_cnt bigint(20) NOT NULL,
  postal_registered_id_max bigint(20) NOT NULL,
  response_queue_name varchar(255) NOT NULL,
  smtp_encryption varchar(255) DEFAULT NULL,
  smtp_host varchar(255) DEFAULT NULL,
  smtp_password varchar(255) DEFAULT NULL,
  smtp_port varchar(255) DEFAULT NULL,
  smtp_username varchar(255) DEFAULT NULL,
  task_queue_name varchar(255) NOT NULL,
  weaver_dir varchar(255) NOT NULL,
  css_id bigint(20) DEFAULT NULL,
  script_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY account_key (account_key),
  UNIQUE KEY name (name),
  KEY FKB9D38A2D26A1F620 (script_id),
  KEY FKB9D38A2DC00E9158 (css_id),
  CONSTRAINT FKB9D38A2DC00E9158 FOREIGN KEY (css_id) REFERENCES css_config (id),
  CONSTRAINT FKB9D38A2D26A1F620 FOREIGN KEY (script_id) REFERENCES script_config (id)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table attachment
--

DROP TABLE IF EXISTS attachment;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE attachment (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  active bit(1) NOT NULL,
  box int(11) DEFAULT NULL,
  color_mode varchar(255) NOT NULL,
  fold bit(1) NOT NULL,
  attachment_key varchar(255) NOT NULL,
  name varchar(255) DEFAULT NULL,
  page_count int(11) DEFAULT NULL,
  pre_press bit(1) NOT NULL,
  sheet_count int(11) DEFAULT NULL,
  fk_account bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY attachment_key (attachment_key),
  KEY FK8AF759235B5A3B3C (fk_account),
  CONSTRAINT FK8AF759235B5A3B3C FOREIGN KEY (fk_account) REFERENCES account (id)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table css_config
--

DROP TABLE IF EXISTS css_config;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE css_config (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  create_timestamp datetime NOT NULL,
  update_timestamp datetime DEFAULT NULL,
  username varchar(255) NOT NULL,
  content longtext,
  fk_account bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FKDFB44FE5B5A3B3C (fk_account),
  CONSTRAINT FKDFB44FE5B5A3B3C FOREIGN KEY (fk_account) REFERENCES account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table custom_template
--

DROP TABLE IF EXISTS custom_template;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE custom_template (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  language varchar(2) NOT NULL,
  name varchar(60) NOT NULL,
  source longtext,
  fk_account bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY fk_account (fk_account,name,language),
  KEY FKE4A0C1885B5A3B3C (fk_account),
  CONSTRAINT FKE4A0C1885B5A3B3C FOREIGN KEY (fk_account) REFERENCES account (id)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table customisable_message
--

DROP TABLE IF EXISTS customisable_message;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE customisable_message (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  msg_key varchar(60) NOT NULL,
  msg_locale varchar(2) NOT NULL,
  msg_value varchar(255) DEFAULT NULL,
  fk_account bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY fk_account (fk_account,msg_key,msg_locale),
  KEY FK83C711FD5B5A3B3C (fk_account),
  CONSTRAINT FK83C711FD5B5A3B3C FOREIGN KEY (fk_account) REFERENCES account (id)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table document
--

DROP TABLE IF EXISTS document;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE document (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  document_id varchar(255) NOT NULL,
  original_filename varchar(255) NOT NULL,
  repos_id varchar(40) DEFAULT NULL,
  fk_document_set bigint(20) NOT NULL,
  fk_print bigint(20) DEFAULT NULL,
  fk_reprint bigint(20) DEFAULT NULL,
  list_index int(11) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY repos_id (repos_id),
  KEY FK335CD11BEE3A501B (fk_document_set),
  KEY FK335CD11B78C0ADA5 (fk_reprint),
  KEY FK335CD11B8582BD78 (fk_print),
  KEY document_doc_id_idx (document_id),
  KEY document_orig_fn_idx (original_filename),
  CONSTRAINT FK335CD11B8582BD78 FOREIGN KEY (fk_print) REFERENCES print_instruction (id),
  CONSTRAINT FK335CD11B78C0ADA5 FOREIGN KEY (fk_reprint) REFERENCES print_instruction (id),
  CONSTRAINT FK335CD11BEE3A501B FOREIGN KEY (fk_document_set) REFERENCES document_set (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table document_set
--

DROP TABLE IF EXISTS document_set;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE document_set (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  timestamp datetime NOT NULL,
  charge_to varchar(255) DEFAULT NULL,
  document_date date DEFAULT NULL,
  document_set_id varchar(255) NOT NULL,
  due_date date NOT NULL,
  language varchar(17) NOT NULL,
  reference varchar(255) DEFAULT NULL,
  subtype varchar(255) DEFAULT NULL,
  type varchar(255) NOT NULL,
  fk_att1 bigint(20) DEFAULT NULL,
  fk_att2 bigint(20) DEFAULT NULL,
  fk_att3 bigint(20) DEFAULT NULL,
  fk_proc_instr bigint(20) NOT NULL,
  fk_receiver bigint(20) NOT NULL,
  fk_sender bigint(20) NOT NULL,
  fk_task bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY fk_task (fk_task,document_set_id),
  KEY FKA835B73E378DC32 (fk_att2),
  KEY FKA835B73E378DC31 (fk_att1),
  KEY FKA835B73EAE49893C (fk_receiver),
  KEY FKA835B73E378DC33 (fk_att3),
  KEY FKA835B73EA3BBA948 (fk_sender),
  KEY FKA835B73E1F9B7F68 (fk_task),
  KEY FKA835B73ECF0AF4F9 (fk_proc_instr),
  KEY document_set_charge_to_idx (charge_to),
  KEY document_set_language_idx (language),
  KEY document_set_reference_idx (reference),
  KEY document_set_docsetid_idx (document_set_id),
  KEY document_set_type_idx (type),
  KEY document_set_subtype_idx (subtype),
  CONSTRAINT FKA835B73ECF0AF4F9 FOREIGN KEY (fk_proc_instr) REFERENCES processing_instructions (id),
  CONSTRAINT FKA835B73E1F9B7F68 FOREIGN KEY (fk_task) REFERENCES task (id),
  CONSTRAINT FKA835B73E378DC31 FOREIGN KEY (fk_att1) REFERENCES attachment (id),
  CONSTRAINT FKA835B73E378DC32 FOREIGN KEY (fk_att2) REFERENCES attachment (id),
  CONSTRAINT FKA835B73E378DC33 FOREIGN KEY (fk_att3) REFERENCES attachment (id),
  CONSTRAINT FKA835B73EA3BBA948 FOREIGN KEY (fk_sender) REFERENCES sender (id),
  CONSTRAINT FKA835B73EAE49893C FOREIGN KEY (fk_receiver) REFERENCES receiver (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table email_template
--

DROP TABLE IF EXISTS email_template;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE email_template (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  email_templ_key varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY email_templ_key (email_templ_key)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table end_customer_view_column
--

DROP TABLE IF EXISTS end_customer_view_column;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE end_customer_view_column (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  column_key varchar(255) NOT NULL,
  column_position int(11) NOT NULL,
  enabled bit(1) NOT NULL,
  visible bit(1) NOT NULL,
  fk_account bigint(20) NOT NULL,
  PRIMARY KEY (id),
  KEY FK1F387C335B5A3B3C (fk_account),
  CONSTRAINT FK1F387C335B5A3B3C FOREIGN KEY (fk_account) REFERENCES account (id)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table end_customer_view_message
--

DROP TABLE IF EXISTS end_customer_view_message;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE end_customer_view_message (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  message_key varchar(255) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table envelope_instruction
--

DROP TABLE IF EXISTS envelope_instruction;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE envelope_instruction (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  envelope_type varchar(20) DEFAULT NULL,
  ei_key varchar(255) DEFAULT NULL,
  pb_nr bit(1) NOT NULL,
  seal varchar(10) DEFAULT NULL,
  window varchar(5) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY ei_key (ei_key),
  KEY env_instr_seal_idx (seal),
  KEY env_instr_window_idx (window),
  KEY env_instr_env_type_idx (envelope_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table image
--

DROP TABLE IF EXISTS image;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE image (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  content_type varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  fk_account bigint(20) NOT NULL,
  PRIMARY KEY (id),
  KEY FK5FAA95B5B5A3B3C (fk_account),
  CONSTRAINT FK5FAA95B5B5A3B3C FOREIGN KEY (fk_account) REFERENCES account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table language
--

DROP TABLE IF EXISTS language;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE language (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  display_name varchar(255) NOT NULL,
  iso_code varchar(2) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY display_name (display_name),
  UNIQUE KEY iso_code (iso_code),
  UNIQUE KEY display_name_2 (display_name,iso_code)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table organisation
--

DROP TABLE IF EXISTS organisation;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE organisation (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  box_nr varchar(255) DEFAULT NULL,
  building varchar(255) DEFAULT NULL,
  city varchar(255) NOT NULL,
  country varchar(255) NOT NULL,
  nr varchar(255) NOT NULL,
  po_box_nr varchar(255) DEFAULT NULL,
  postal_code varchar(255) NOT NULL,
  region varchar(255) DEFAULT NULL,
  street varchar(255) NOT NULL,
  company varchar(255) NOT NULL,
  department varchar(255) DEFAULT NULL,
  language varchar(17) DEFAULT NULL,
  fk_contact_person bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FK3A5300DA61114687 (fk_contact_person),
  CONSTRAINT FK3A5300DA61114687 FOREIGN KEY (fk_contact_person) REFERENCES person (id)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table person
--

DROP TABLE IF EXISTS person;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE person (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  box_nr varchar(255) DEFAULT NULL,
  building varchar(255) DEFAULT NULL,
  city varchar(255) NOT NULL,
  country varchar(255) NOT NULL,
  nr varchar(255) NOT NULL,
  po_box_nr varchar(255) DEFAULT NULL,
  postal_code varchar(255) NOT NULL,
  region varchar(255) DEFAULT NULL,
  street varchar(255) NOT NULL,
  email varchar(255) DEFAULT NULL,
  first_name varchar(255) NOT NULL,
  language varchar(17) DEFAULT NULL,
  last_name varchar(255) NOT NULL,
  middle_name varchar(255) DEFAULT NULL,
  suffix varchar(255) DEFAULT NULL,
  title varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table post_instruction
--

DROP TABLE IF EXISTS post_instruction;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE post_instruction (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  delivery_type varchar(12) DEFAULT NULL,
  fk_env_instr bigint(20) NOT NULL,
  PRIMARY KEY (id),
  KEY FK1CD9F36FBA6F9F1A (fk_env_instr),
  KEY post_instr_delivery_type_idx (delivery_type),
  CONSTRAINT FK1CD9F36FBA6F9F1A FOREIGN KEY (fk_env_instr) REFERENCES envelope_instruction (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table print_instruction
--

DROP TABLE IF EXISTS print_instruction;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE print_instruction (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  color_mode varchar(12) DEFAULT NULL,
  copy_layer bit(1) NOT NULL,
  duplex_mode varchar(12) DEFAULT NULL,
  layer varchar(255) DEFAULT NULL,
  print_mode varchar(12) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY print_instr_color_mode_idx (color_mode),
  KEY print_instr_duplex_mode_idx (duplex_mode),
  KEY print_instr_layer_idx (layer),
  KEY print_instr_print_mode_idx (print_mode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table print_job
--

DROP TABLE IF EXISTS print_job;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE print_job (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  timestamp datetime NOT NULL,
  accounting_file varchar(255) DEFAULT NULL,
  log_file varchar(255) DEFAULT NULL,
  mail_id_file varchar(255) DEFAULT NULL,
  name varchar(60) NOT NULL,
  postal_customer_file_ref varchar(255) NOT NULL,
  postal_deposit_ref varchar(255) NOT NULL,
  postal_drop_date date DEFAULT NULL,
  postal_mailing_ref varchar(255) NOT NULL,
  registered_mail_id_file varchar(255) DEFAULT NULL,
  status varchar(255) NOT NULL,
  fk_account bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY name (name),
  KEY FK94E95F2B5B5A3B3C (fk_account),
  KEY print_job_name_idx (name),
  CONSTRAINT FK94E95F2B5B5A3B3C FOREIGN KEY (fk_account) REFERENCES account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table print_job_files
--

DROP TABLE IF EXISTS print_job_files;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE print_job_files (
  print_job_id bigint(20) NOT NULL,
  file_name varchar(255) DEFAULT NULL,
  KEY FK7CBD306357B117A3 (print_job_id),
  CONSTRAINT FK7CBD306357B117A3 FOREIGN KEY (print_job_id) REFERENCES print_job (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table print_job_unit
--

DROP TABLE IF EXISTS print_job_unit;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE print_job_unit (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  blue_sheet_count int(11) NOT NULL,
  buff_sheet_count int(11) NOT NULL,
  color_mode varchar(255) NOT NULL,
  envelope_type varchar(255) NOT NULL,
  goldenrod_sheet_count int(11) NOT NULL,
  gray_sheet_count int(11) NOT NULL,
  green_sheet_count int(11) NOT NULL,
  ivory_sheet_count int(11) NOT NULL,
  orange_sheet_count int(11) NOT NULL,
  page_count int(11) NOT NULL,
  pink_sheet_count int(11) NOT NULL,
  postal_mail_id bigint(20) NOT NULL,
  red_sheet_count int(11) NOT NULL,
  sheet_count int(11) NOT NULL,
  white_sheet_count int(11) NOT NULL,
  yellow_sheet_count int(11) NOT NULL,
  fk_document_set bigint(20) NOT NULL,
  fk_print_job bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY fk_print_job (fk_print_job,fk_document_set),
  KEY FK7FEBF618EE3A501B (fk_document_set),
  KEY FK7FEBF618DE7ECD25 (fk_print_job),
  CONSTRAINT FK7FEBF618DE7ECD25 FOREIGN KEY (fk_print_job) REFERENCES print_job (id),
  CONSTRAINT FK7FEBF618EE3A501B FOREIGN KEY (fk_document_set) REFERENCES document_set (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table processing_instructions
--

DROP TABLE IF EXISTS processing_instructions;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE processing_instructions (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  archive bit(1) NOT NULL,
  pi_key varchar(255) DEFAULT NULL,
  fk_post bigint(20) DEFAULT NULL,
  fk_print bigint(20) DEFAULT NULL,
  fk_reprint bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY pi_key (pi_key),
  KEY FKC654375178C0ADA5 (fk_reprint),
  KEY FKC65437518582BD78 (fk_print),
  KEY FKC6543751F8FDBC04 (fk_post),
  CONSTRAINT FKC6543751F8FDBC04 FOREIGN KEY (fk_post) REFERENCES post_instruction (id),
  CONSTRAINT FKC654375178C0ADA5 FOREIGN KEY (fk_reprint) REFERENCES print_instruction (id),
  CONSTRAINT FKC65437518582BD78 FOREIGN KEY (fk_print) REFERENCES print_instruction (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table receiver
--

DROP TABLE IF EXISTS receiver;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE receiver (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  email_address varchar(255) DEFAULT NULL,
  mail_id bit(1) NOT NULL,
  receiver_id varchar(255) DEFAULT NULL,
  relation_type varchar(255) NOT NULL,
  fk_organisation bigint(20) DEFAULT NULL,
  fk_person bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FKCFCBE9EFBEA1D812 (fk_organisation),
  KEY FKCFCBE9EF9982AB88 (fk_person),
  CONSTRAINT FKCFCBE9EF9982AB88 FOREIGN KEY (fk_person) REFERENCES person (id),
  CONSTRAINT FKCFCBE9EFBEA1D812 FOREIGN KEY (fk_organisation) REFERENCES organisation (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table script_config
--

DROP TABLE IF EXISTS script_config;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE script_config (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  create_timestamp datetime NOT NULL,
  update_timestamp datetime DEFAULT NULL,
  username varchar(255) NOT NULL,
  content longtext,
  fk_account bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY FKDF2E45365B5A3B3C (fk_account),
  CONSTRAINT FKDF2E45365B5A3B3C FOREIGN KEY (fk_account) REFERENCES account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table sender
--

DROP TABLE IF EXISTS sender;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE sender (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  sender_key varchar(255) NOT NULL,
  fk_account bigint(20) NOT NULL,
  fk_organisation bigint(20) DEFAULT NULL,
  fk_person bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY fk_account (fk_account,sender_key),
  KEY FKCA001A35BEA1D812 (fk_organisation),
  KEY FKCA001A359982AB88 (fk_person),
  KEY FKCA001A355B5A3B3C (fk_account),
  KEY sender_senderkey_idx (sender_key),
  CONSTRAINT FKCA001A355B5A3B3C FOREIGN KEY (fk_account) REFERENCES account (id),
  CONSTRAINT FKCA001A359982AB88 FOREIGN KEY (fk_person) REFERENCES person (id),
  CONSTRAINT FKCA001A35BEA1D812 FOREIGN KEY (fk_organisation) REFERENCES organisation (id)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table task
--

DROP TABLE IF EXISTS task;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE task (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  created_by varchar(255) NOT NULL,
  created_on datetime NOT NULL,
  original_filename varchar(255) NOT NULL,
  task_id varchar(255) NOT NULL,
  fk_account bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY fk_account (fk_account,task_id),
  KEY FK3635855B5A3B3C (fk_account),
  KEY task_taskid_idx (task_id),
  KEY task_orig_fn_idx (original_filename),
  CONSTRAINT FK3635855B5A3B3C FOREIGN KEY (fk_account) REFERENCES account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table user
--

DROP TABLE IF EXISTS user;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE user (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  timestamp datetime NOT NULL,
  confirmation_code varchar(255) DEFAULT NULL,
  email_address varchar(255) NOT NULL,
  language varchar(2) DEFAULT NULL,
  last_login date DEFAULT NULL,
  password_hash varchar(255) NOT NULL,
  state varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY email_address (email_address)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table user_account
--

DROP TABLE IF EXISTS user_account;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE user_account (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  timestamp datetime NOT NULL,
  allow_ads bit(1) NOT NULL,
  receiver_id varchar(255) DEFAULT NULL,
  role varchar(255) NOT NULL,
  state varchar(255) NOT NULL,
  fk_account bigint(20) NOT NULL,
  fk_user bigint(20) NOT NULL,
  PRIMARY KEY (id),
  KEY fk_ua_user_exists (fk_user),
  KEY fk_ua_account_exists (fk_account),
  CONSTRAINT fk_ua_account_exists FOREIGN KEY (fk_account) REFERENCES account (id),
  CONSTRAINT fk_ua_user_exists FOREIGN KEY (fk_user) REFERENCES user (id)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;