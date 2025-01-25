---
pill: Config from stdin
subtitle: don't do this
---

You can provide configuration options to the standard input of the Flyway command line, using the
`-configFiles=-` option. Flyway will expect such configuration to be in the same format as a configuration file.

This allows you to compose Flyway with other operations. For instance, you can decrypt a config file containing
login credentials and pipe it straight into Flyway.

#### Examples

Read a single option from `echo`:
```powershell
echo $'flyway.url=jdbc:h2:mem:mydb' | flyway info -configFiles=-
```

Read multiple options from `echo`, delimited by newlines:
```powershell
echo $'flyway.url=jdbc:h2:mem:mydb\nflyway.user=sa' | flyway info -configFiles=-
```

Use `cat` to read a config file and pipe it directly into Flyway:
```powershell
cat flyway.conf | flyway migrate -configFiles=-
```

Use `gpg` to encrypt a config file, then pipe it into Flyway.

Encrypt the config file:
```powershell
gpg -e -r "Your Name" flyway.conf
```

Decrypt the file and pipe it to Flyway:
```powershell
gpg -d -q flyway.conf.gpg | flyway info -configFiles=-
```
