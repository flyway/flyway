--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE TABLE network_device_day_start_state
(   segment_Id          INTEGER DEFAULT 1 NOT NULL,
    day                 TIMESTAMP,
    net_device_id       INTEGER,
    uiq_device_state_id INTEGER,
    PRIMARY KEY (day, net_device_id))
ORGANIZATION INDEX
INCLUDING uiq_device_state_id
OVERFLOW
;