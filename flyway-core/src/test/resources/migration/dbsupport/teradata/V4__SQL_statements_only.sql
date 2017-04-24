-- A simple SQL script
INSERT INTO financial.accts VALUES(2, 'az', '1234567890123456', 20170101, 20171231);
INSERT INTO financial.accts VALUES(3, 'az', '1234567890123456', 20170101, 20171231);

DELETE financial.accts WHERE cust_id = 2;
DELETE financial.accts WHERE cust_id = 3;

