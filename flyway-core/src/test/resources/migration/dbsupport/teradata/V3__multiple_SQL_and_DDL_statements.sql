-- Execute multiple DDL statements and SQL statements
INSERT INTO financial.accts VALUES(1, 'az', '1234567890123456', 20170101, 20171231);

CREATE SET TABLE financial.checking_acct ,NO FALLBACK ,
     NO BEFORE JOURNAL,
     NO AFTER JOURNAL,
     CHECKSUM = DEFAULT,
     DEFAULT MERGEBLOCKRATIO
     (
      cust_id INTEGER NOT NULL,
      acct_nbr CHAR(16) CHARACTER SET LATIN NOT CASESPECIFIC,
      minimum_balance INTEGER,
      per_check_fee DECIMAL(9,2) FORMAT '--Z(6)9.99',
      account_active CHAR(1) CHARACTER SET LATIN NOT CASESPECIFIC,
      acct_start_date DATE FORMAT 'YYYYMMDD',
      acct_end_date DATE FORMAT 'YYYYMMDD',
      starting_balance DECIMAL(9,2) FORMAT '--Z(6)9.99',
      ending_balance DECIMAL(9,2) FORMAT '--Z(6)9.99')
UNIQUE PRIMARY INDEX ( cust_id );

INSERT INTO financial.checking_acct VALUES (1, '1234567890123456', 0, 1, 'A', 20170101, NULL, 100, 0);

CREATE SET TABLE financial.checking_tran ,NO FALLBACK ,
     NO BEFORE JOURNAL,
     NO AFTER JOURNAL,
     CHECKSUM = DEFAULT,
     DEFAULT MERGEBLOCKRATIO
     (
      Tran_Id INTEGER NOT NULL,
      Cust_Id INTEGER NOT NULL,
      Acct_Nbr CHAR(16) CHARACTER SET LATIN NOT CASESPECIFIC,
      Channel_Nbr INTEGER,
      Session_Id INTEGER,
      Check_Nbr SMALLINT,
      Tran_Duration SMALLINT,
      Tran_Amt DECIMAL(9,2),
      Principal_Amt DECIMAL(9,2),
      Interest_Amt DECIMAL(9,2),
      New_Balance DECIMAL(9,2),
      Tran_Date DATE FORMAT 'YYYY-MM-DD',
      Tran_Time CHAR(6) CHARACTER SET LATIN NOT CASESPECIFIC,
      Channel CHAR(1) CHARACTER SET LATIN NOT CASESPECIFIC,
      Tran_Code CHAR(2) CHARACTER SET LATIN NOT CASESPECIFIC)
PRIMARY INDEX ( Tran_Id );
