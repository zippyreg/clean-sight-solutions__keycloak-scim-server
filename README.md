#### This project is based on [Metatavu/keycloak-scim-server](https://github.com/Metatavu/keycloak-scim-server)

``` 
**TODO**: Update readme with more complete documentation
```
---

# Keycloak SCIM 2.0 Extension

This project provides a **SCIM 2.0-compliant extension** for [Keycloak](https://www.keycloak.org/), enabling SCIM-based user and group provisioning. It supports:

- **Realm-level SCIM APIs**:  
  `/realms/{realm}/scim/v2`
- **Organization-level SCIM APIs** (Keycloak 26+ with Organizations):  
  `/realms/{realm}/scim/v2/organizations/{organizationId}`

## Prerequisites

- **Keycloak**: This extension is developed for Keycloak **26.1.4**. It may work with other versions, but compatibility is not guaranteed.
- **Java**: Java **21** is required to build the project.

## Installation

### Option 1: Install from GitHub Packages (recommended)

Easiest way to use the extension is to download a JAR file from GitHub packages. 

1. Download the latest release from: [GitHub](https://github.com/zippyreg/keycloak-scim-server/releases/latest)
2. Copy it to your Keycloak instance:
```bash
   cp keycloak-scim-server-*.jar $KEYCLOAK_HOME/providers/
```
3. Restart Keycloak.


### Option 2: Build from Source (with tests)

1. Build the extension:
```bash
./gradlew build
```
2. Copy the built JAR file from `build/libs/keycloak-scim-server-<version>.jar` to the Keycloak providers directory:
```bash
cp build/libs/keycloak-scim-server-*.jar $KEYCLOAK_HOME/providers/
```

### Option 3: Build from Source (excluding tests)

If you're really in a hurry (or short on resources), you can exclude the test run and just build a deployable JAR.

> [!CAUTION]
> This will not validate that the SCIM server still passes compliance and the resulting JAR may have issues that
> are not seen until deployment. It's unlikely, but possible. All release binaries will be tested and therefore
> SCIM 2.0 compliant.

1. Build the extension:
```bash
./gradlew build -x test
```
2. Copy the built JAR file from `build/libs/keycloak-scim-server-<version>.jar` to the Keycloak providers directory:
```bash
cp build/libs/keycloak-scim-server-*.jar $KEYCLOAK_HOME/providers/
```

## Configuration

### Configuration on Instance level

Configuration on instance level is done by defining environment variables in the Keycloak server. 

The following environment variables are available:
| Setting                  | Value                                                                             |
| ------------------------ | --------------------------------------------------------------------------------- |
| SCIM_AUTHENTICATION_MODE | Authentication mode for SCIM API. Possible values are KEYCLOAK and EXTERNAL. If the value is not set the server will respond unauthorzed for all requests. |
| SCIM_EXTERNAL_ISSUER     | Issuer for the external authentication. This is used to validate the JWT token.   |
| SCIM_EXTERNAL_AUDIENCE   | JWKS URI for the external authentication. This is used to validate the JWT token. |
| SCIM_EXTERNAL_JWKS_URI   | Audience for the external authentication. This is used to validate the JWT token. |

### Configuration on Realm level

The following REST call can be called through the Keycloak Admin API to store the settings under realm attributes. 

PUT `/admin/realms/{realm}`
```
{
  "attributes": {
    "scim.authentication.mode": "EXTERNAL|INTERNAL",
    "scim.external.issuer": "string",
    "scim.external.jwks.uri": "string",
    "scim.external.audience": "string"
  }
}
```

### Configuration on Organization level

Configuration on organization level is done by defining organization attributes in the Keycloak server.
The following organization attributes are available:

| Setting                    | Value                                                                                                                                                                                                                                |
|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SCIM_AUTHENTICATION_MODE   | Authentication mode for SCIM API. Possible values are KEYCLOAK and EXTERNAL. If the value is not set the server will respond unauthorzed for all requests. Currently on organization level only EXTERNAL is supported.               |
| SCIM_EXTERNAL_ISSUER       | Issuer for the external authentication. This is used to validate the JWT token.                                                                                                                                                      |
| SCIM_EXTERNAL_AUDIENCE     | JWKS URI for the external authentication. This is used to validate the JWT token.                                                                                                                                                    |
| SCIM_EXTERNAL_JWKS_URI     | Audience for the external authentication. This is used to validate the JWT token.                                                                                                                                                    |
| SCIM_LINK_IDP              | Enables support for linking organization identity provider with user.                                                                                                                                                                |
| SCIM_EMAIL_AS_USERNAME     | Forces server to user email as username instead of actual username. When this setting is enabled username will be unaffected by any update operations. This setting is currently supported only in organization level configuration  |


## License

[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
  
---

<div id="metatavu-custom-footer"><div align="center">
    <img src="https://raw.githubusercontent.com/zippyreg/clean-sight-solutions__keycloak-scim-server/refs/heads/develop/public/black-no-bg.jpg" alt="Organization Logo" width="120">
    <p>© 2025 ARE Event Productions. All rights reserved.</p>
    <p>
        <a href="https://www.areep.com">Website</a> | 
        <a href="https://www.facebook.com/are.event.productions/">Facebook</a>
    </p>
</div></div>
