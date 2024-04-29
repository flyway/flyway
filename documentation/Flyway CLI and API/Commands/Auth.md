---
subtitle: Auth
---
# Auth

`auth` initiates an online or offline license acquisition flow that can be used instead of the legacy [license key](Configuration/Parameters/Flyway/License Key) workflow.

If `auth` is run on a machine that is connected to the internet with an accessible browser, Flyway will launch a web browser prompting the user to log in with their Redgate account
username and password. Please note that `auth` is not supported on WSL, nor when using SSH. For these cases and any where a web browser is inaccessible on the same machine that is
running Flyway, it is recommended to use environment variables (described below) to authorize Flyway.

Upon successful login, a license permit is then saved to disk in the `Flyway CLI` directory of the Redgate app data folder. If the user is offline, a
[link to create a license permit](https://permits.red-gate.com/offline?productCode=63) will be printed to the console. The user can then copy the license permit
from that site and paste it into the console to authorize Flyway.

#### App Data Folder Locations

###### Windows

You can find the `Flyway CLI` directory here: `%APPDATA%\Redgate\Flyway CLI`.

###### MacOS & Linux

You can find the `Flyway CLI` directory here: `~/.config/Redgate/Flyway CLI`.

#### Flags:

| Parameter             | Edition            |  Description
|-----------------------|--------------------| -----------------------------------------------------
| -IAgreeToTheEula      | All                | Mandatory for `auth` to run
| -logout               | All                | Logs a user out of their authorized session of Flyway
| -startTeamsTrial      | All                | Starts a Teams trial
| -startEnterpriseTrial | All                | Starts an Enterprise trial

#### Personal Access Tokens (PATs)

Users can specify a Redgate email and personal access token that can be used to non-interactively authorize Flyway without needing to run the `auth` command. If an email and PAT token
are configured, Flyway can be used normally and will be licensed to the edition of Flyway you have access to assuming you are working in an environment that is connected to the internet.
PATs can be generated at this [link](https://identityprovider.red-gate.com/personaltokens).

Redgate emails and PATs are respectively configured using the `email` and `token` configuration parameters in the TOML configuration file, on the commandline, or by using the `FLYWAY_EMAIL`
and `FLYWAY_TOKEN` environment variables. Here are some examples of how to use PATs:

Commandline:
```
flyway info -email=foo.bar@red-gate.com -token=1234ABCD
```

TOML:
```toml
[flyway]
email = "foo.bar@red-gate.com"
token = "1234ABCD"
```

Environment variables:
```shell
export FLYWAY_EMAIL=foo.bar@red-gate.com
export FLYWAY_TOKEN=1234ABCD
```

Using a personal access token will store a license permit in the Redgate app data folder, the same location as when running the `auth` command.

#### Environment Variables

Users can specify license permits using environment variables that Flyway recognizes without needing to run the `auth` command:

- `REDGATE_LICENSING_PERMIT_PATH`: Specifies a custom path to a license permit file. Users can save a license permit in a text file to an arbitrary location on disk and use this
environment variable to point Flyway to the full path of the license permit file in order to authorize Flyway.
- `REDGATE_LICENSING_PERMIT`: Specifies a license permit as a string. Users can save a license permit as a raw text value to this environment variable to authorize Flyway.

#### Authorization Precedence

There are multiple ways to authorize Flyway. The following list shows the order of precedence for authorization:

1. `REDGATE_LICENSING_PERMIT_PATH` environment variable
2. `REDGATE_LICENSING_PERMIT` environment variable
3. License permit located in the `Flyway CLI` directory of the Redgate app data folder, saved to disk by running the `auth` command or by having run Flyway with an email and PAT specified
at least once (the online interactive flow, non-interactive online personal access token flow, and offline flow all save the license permit to the same location)
4. [License key](Configuration/Parameters/Flyway/License Key)

#### Refresh Tokens

When `auth` is run successfully or a PAT successfully retrieves a license permit, a refresh token is saved to disk in the `Flyway CLI` directory of the Redgate app data folder.
This refresh token is used to automatically refresh a user's license permit when it expires. Each time a license permit is refreshed, a new refresh token is saved to disk,
replacing the existing refresh token. This only applies to expired license permits in the `Flyway CLI` directory of the Redgate app data folder—Flyway will not automatically
refresh expired license permits specified by environment variables.

#### Logout

`auth -logout` can be run to log out of an authorized session of Flyway. Under the hood, logging out simply deletes the license permit and refresh token stored on disk in the
Redgate app data folder. Assuming no other methods of authorization are in use (such as a permit environment variable, a legacy license key environment variable, or a PAT token), the user will be
logged out of their session and Flyway will run as Community Edition. If a valid permit environment variable, legacy license key environment variable, or PAT token is set, Flyway will still
honor these as authorizations and logout will not remove them.
