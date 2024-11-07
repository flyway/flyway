---
subtitle: Auth
---
# Auth

`auth` initiates an online or offline license acquisition flow that can be used instead of the legacy [license key](Configuration/Parameters/Flyway/License Key) workflow.

If `auth` is run on a machine that is connected to the internet with an accessible browser, Flyway will launch a web browser prompting the user to log in with their Redgate account
username and password. Please note that `auth` is not supported on WSL, nor when using SSH. For these cases and any where a web browser is inaccessible on the same machine that is
running Flyway, it is recommended to use an [non-interactive mechanism](<Configuration/Flyway Licensing>) to license Flyway.

Upon successful login, a license permit is then saved to disk in the `Flyway CLI` directory of the Redgate app data folder. If the user is offline, a
[link to create a license permit](https://permits.red-gate.com/offline?productCode=63) will be printed to the console. The user can then copy the license permit
from that site and paste it into the console to authorize Flyway.


## Flags:

| Parameter             | Edition            |  Description
|-----------------------|--------------------| -----------------------------------------------------
| -IAgreeToTheEula      | All                | (Mandatory) By using this option you consent to the [Redgate EULA](https://www.red-gate.com/eula)
| -logout               | All                | Logs a user out of their authorized session of Flyway
| -startTeamsTrial      | All                | Starts a Teams trial
| -startEnterpriseTrial | All                | Starts an Enterprise trial

## Logout

`auth -logout` can be run to log out of an authorized session of Flyway. This will delete the license permit and refresh token stored on disk in the
Redgate app data folder. Assuming no other methods of authorization are in use (such as a permit environment variable, a legacy license key environment variable, or a PAT token), the user will be
logged out of their session and Flyway will run as Community Edition. If a valid permit environment variable, legacy license key environment variable, or PAT token is set, Flyway will still
honor these as authorizations and logout will not remove them.

## App Data Folder Locations

### Windows

You can find the `Flyway CLI` directory here: `%APPDATA%\Redgate\Flyway CLI`.

### MacOS & Linux

You can find the `Flyway CLI` directory here: `~/.config/Redgate/Flyway CLI`.
