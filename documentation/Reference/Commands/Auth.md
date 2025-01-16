---
subtitle: Auth
---

## Description

`auth` initiates an online license acquisition flow.

For an overview of ways to license Flyway see [Flyway Licensing](https://documentation.red-gate.com/flyway/getting-started-with-flyway/system-requirements/licensing).

If `auth` is run on a machine that is connected to the internet with an accessible browser, Flyway will launch a web browser prompting the user to log in with their Redgate account
username and password. 

- `auth` needs to be able to launch a web browser and so doesn't work on WSL or when interacting with Flyway over SSH. For these cases it is recommended to use a [non-interactive mechanism](https://documentation.red-gate.com/flyway/getting-started-with-flyway/system-requirements/licensing) to license Flyway.
- If you need an offline licensing mechanism please look at [License Permits](https://documentation.red-gate.com/flyway/getting-started-with-flyway/system-requirements/licensing/license-permits).

### App Data Folder Locations
Flyway stores permits in a central location rather than it's installation location.

#### Windows

You can find the `Flyway CLI` directory here: `%APPDATA%\Redgate\Flyway CLI`.

#### MacOS & Linux

You can find the `Flyway CLI` directory here: `~/.config/Redgate/Flyway CLI`.

## Usage Examples

### Login
<pre class="console"><span>&gt;</span> flyway auth -IAgreeToTheEula</pre>

### Logout
`auth -logout` can be run to log out of an authorized session of Flyway. This will delete the license permit and refresh token stored on disk in the
Redgate app data folder. Assuming no other methods of authorization are in use (such as a permit environment variable, a legacy license key environment variable, or a PAT token), the user will be
logged out of their session and Flyway will run as Community Edition. If a valid permit environment variable, legacy license key environment variable, or PAT token is set, Flyway will still
honor these as authorizations and logout will not remove them.

## Flags:

| Parameter               | Edition   | Description                                                                                       |
|-------------------------|-----------|---------------------------------------------------------------------------------------------------|
| `-IAgreeToTheEula`      | Community | (Mandatory) By using this option you consent to the [Redgate EULA](https://www.red-gate.com/eula) |
| `-logout`               | Community | Logs a user out of their authorized session of Flyway                                             |
| `-startTeamsTrial`      | Community | Starts a Teams trial                                                                              |
| `-startEnterpriseTrial` | Community | Starts an Enterprise trial                                                                        |



