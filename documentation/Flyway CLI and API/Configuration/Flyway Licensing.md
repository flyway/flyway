---
pill: authentication
---

Flyway has a number of ways you can authenticate yourself to allow you to access features that are otherwise unavailable (for example, the Flyway Service, Teams and Enterprise functionality).

This page signposts you to the various mechanisms available and when you should use them.

_note:_ this is for the Flyway product itself - not your database.

## Online interactive authentication
Online licensing means that your license is automatically kept up to date and once configured, you won't have to manually update your license key when it expires.

### Auth verb
Using the [Auth](<Commands/Auth>) verb will use a browser pop-up on your computer to authenticate the product. Typically this would be done once and thereafter Flyway can refresh the credentials automatically.

## Online non-interactive authentication
### Access Token (PAT)
Where you need a non-interactive flow (for example in a pipeline) then you can create a token in the [Redgate portal](https://identityprovider.red-gate.com/personaltokens) and use that with your registered email address to authenticate Flyway.
- [token](<Configuration/Parameters/Flyway/Token>) parameter
- [email](<Configuration/Parameters/Flyway/Email>) parameter

Using a personal access token will store a license permit in the Redgate app data folder, the same location as when running the [Auth](<Commands/Auth>) command.

## Offline non-interactive authentication
These work without contacting Redgate (e.g. air-gapped pipelines) and so will expire at renewal time. This will require that you update your pipeline to refresh the key/permit to continue working.

### License permits
- [License Permits](<Configuration/License Permits>)

### Legacy license keys
- [License Key](<Configuration/Parameters/Flyway/License Key>)

## Authorization Precedence

There are multiple ways to authorize Flyway. The following list shows the order of precedence for authorization if you attempt to use more than one:

1. Permit Location set in Environment Variable - [License permit](<Configuration/License Permits>)
2. Permit as an Environment Variable - [License permit](<Configuration/License Permits>)
3. Online Authentication by running the [Auth](<Commands/Auth>) command.
4. Access token defined with an `email` and `token` specified.
5. Legacy Flyway [License key](Configuration/Parameters/Flyway/License Key)

## Token Refresh

When an online authentication process successfully retrieves a license permit, a refresh token is saved to disk in the `Flyway CLI` directory of the Redgate app data folder.

This refresh token is used to automatically refresh a user's license permit when it expires. Each time a license permit is refreshed, a new refresh token is saved to disk, replacing the existing refresh token.

This only applies to expired license permits in the `Flyway CLI` directory of the Redgate app data folder—Flyway cannot automatically refresh expired license permits specified using offline [License Permits](<Configuration/License Permits>).