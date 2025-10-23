---
subtitle: Redgate Regex Rules Library
---
{% include enterprise.html %}

{% include anchor.html link="RX001"%}
# Rule: RX001 DROP TABLE 
Dropping a table is likely to result in the loss of data so should be investigated before continuing.
### Dialects supported: `all`
---

{% include anchor.html link="RX002"%}
# Rule: RX002 Attempt to change password
Changing passwords through a DB migration is not considered best practice
### Dialects supported: `oracle/postgres/tsql`
---

{% include anchor.html link="RX003"%}
# Rule: RX003 TRUNCATE statement used
This operation is likely to result in a loss of data so should be investigated before continuing
### Dialects supported: `all`
---

{% include anchor.html link="RX004"%}
# Rule: RX004 DROP COLUMN statement used
This operation is likely to result in a loss of data so should be investigated before continuing
### Dialects supported: `all`
---

{% include anchor.html link="RX005"%}
# Rule: RX005 GRANT TO PUBLIC statement used
It is not common to access to this degree so should be investigated before continuing
### Dialects supported: `all`
---

{% include anchor.html link="RX006"%}
# Rule: RX006 GRANT WITH GRANT OPTION statement used
Allows grantee to grant additional permissions and so it becomes difficult to track the scope of permissions
### Dialects supported: `all`
---

{% include anchor.html link="RX007"%}
# Rule: RX007 GRANT WITH ADMIN OPTION statement used
Allows grantee to grant administrative permissions and so it becomes difficult to control the scope of permissions
### Dialects supported: `all`
---

{% include anchor.html link="RX008"%}
# Rule: RX008 ALTER USER statement used
Modifies the properties of an existing user and should be investigated before continuing
### Dialects supported: `all`
---

{% include anchor.html link="RX009"%}
# Rule: RX009 GRANT ALL statement used
It is not common to access to this degree so should be investigated before continuing
### Dialects supported: `all`
---

{% include anchor.html link="RX010"%}
# Rule: RX010 CREATE ROLE statement used
This is used to create user accounts so should be investigated before continuing
### Dialects supported: `all`
---

{% include anchor.html link="RX011"%}
# Rule: RX011 ALTER ROLE statement used
This is used to modify user accounts so should be investigated before continuing
### Dialects supported: `all`
---

{% include anchor.html link="RX012"%}
# Rule: RX012 DROP PARTITION statement used
This is likely to result in a loss of data so should be investigated before continuing
### Dialects supported: `all`
---

{% include anchor.html link="RX013"%}
# Rule: RX013 CREATE TABLE statement without a PRIMARY KEY constraint
This could lead to performance problems
### Dialects supported: `all`
---

{% include anchor.html link="RX015"%}
# Rule: RX014 _No Table Description_
A table has been created but has no `MS_Description` property added

It is a good practice to include a description in the `MS_Description` extended property to document the purpose of a table.
### Dialects supported: `TSQL`




