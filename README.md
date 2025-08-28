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

### Azure Entra ID SCIM Configuration

This extension is compatible with **Microsoft Entra ID** SCIM provisioning.

#### Keycloak Configuration

Before Entra ID can provision users and groups to Keycloak via SCIM, you need to configure SCIM authentication settings.

These settings can be applied either:

* At the realm level (for /realms/{realm}/scim/v2)
* Or at the organization level (for /realms/organizations/scim/v2/organizations/{organizationId})

For more details, refer to the sections [Configuration on Realm Level] and [Configuration on Organization Level in this document].

SCIM Settings for Entra ID

When using Entra ID settings will be following:

| Setting                  | Value                                                                         |
| ------------------------ | ----------------------------------------------------------------------------- |
| SCIM_AUTHENTICATION_MODE | ```EXTERNAL```                                                                |
| SCIM_EXTERNAL_ISSUER     | ```https://sts.windows.net/<your-tenant-id>/```                               |
| SCIM_EXTERNAL_AUDIENCE   | ```8adf8e6e-67b2-4cf2-a259-e3dc5476c621```                                    |
| SCIM_EXTERNAL_JWKS_URI   | ```https://login.microsoftonline.com/<your-tenant-id>/discovery/v2.0/keys```  |

Replace <your-tenant-id> with your actual Azure tenant ID.

* SCIM_AUTHENTICATION_MODE enables external authentication support for the SCIM server. In this case the external authentication source will be the Azure Entra ID.
* SCIM_EXTERNAL_ISSUER ensures the JWT token was issued by your tenant.
* SCIM_EXTERNAL_AUDIENCE must be exactly 8adf8e6e-67b2-4cf2-a259-e3dc5476c621 — this is the default audience used by Entra ID for non-gallery applications.
* SCIM_EXTERNAL_JWKS_URI allows Keycloak to fetch public keys for token validation.

#### Azure Configuration

Step-by-step guide on the Azure:

1. Sign in to the [Azure portal](https://portal.azure.com)
2. Go to **Identity → Applications → Enterprise applications**
3. Click **+ New application → + Create your own application**
4. Enter a name for your application (e.g., My Keycloak SCIM).
5. Choose **Integrate any other application you don't find in the gallery.**
6. Click **Create** to create the application. The application will open automatically in its management screen.
7. In the application's left-hand menu, select **Provisioning**.
8. Click **+ New configuration**.
9. Fill in the following:
 - Tenant URL (realm): https://mykeycloak.example.com/realms/my-realm/scim/v2 or 
 - Tenant URL (organization): https://mykeycloak.example.com/realms/my-realm/scim/v2/organizations/{organizationId} 
 - Secret Token: Leave this field empty (the application will use the Entra ID bearer token).
10. Click **Test Connection** to verify the SCIM endpoint.
11. Click **Create**.
12. Navigate to **Attribute Mapping (Preview)**.
13. Open **Provision Microsoft Entra ID Groups**.
14. Set **Enabled** to **No**.
15. Click **Save**.
16. Go back → **open Provision Microsoft Entra ID Users**.
17. Open Provision Microsoft Entra ID Users.
18. Define mappings, following are required for Keycloak extension:
- userName
- active
- emails[type eq "work"].value
- name.givenName
- name.familyName
19. Click Save.
20. Go back to Provisioning.
21. Set Provisioning Status to On.
22. Click Save.
23. Reload the page to ensure the configuration was saved.
24. Navigate to **Manage > Users and groups > + Add user/group**.
25.  Select the user you want to provision and click Assign.
26. Navigate to **Provision on demand**.
27. Find the user you just assigned.
28. Click on the user and select **Provision**.
29. Verify that the provisioning completes successfully.

For more information, refer to the following documents: 

https://learn.microsoft.com/en-us/entra/identity/saas-apps/tutorial-list

#### Identity Provider Linking with Azure Entra ID

Identity Provider linking with Entra ID requires a few additional configuration steps on both the Entra and Keycloak sides.

**Step 1: Add externalId**

In the Keycloak admin console, ensure that you have externalId attribute defined in your Realm Settings > User Profile. This attribute is used to store user's external id in the Keycloak side and without it the Identity Provider linking will fail. 

**Step 2: Map externalId in SCIM provisioning**

In the Entra Id, make sure that the objectId from Entra ID is mapped into the SCIM externalId field:

1. Navigate to your **Enterprise Application** > **Provisioning** > **Attribute Mapping (Preview)** > **Provision Microsoft Entra ID Users**.
2. Click **Add New Mapping**.
3. Set:
  - **Source attribute**: objectId
  - **Target attribute**: externalId
4. Click **Save**.

This ensures that during SCIM provisioning, the Entra objectId is stored in Keycloak as the user’s externalId, which will later be used for identity linking.

**Step 3: Configure Keycloak Identity Provider to Use Object ID**

Next, configure your Entra ID Identity Provider in Keycloak to use the oid claim from the login token instead of the default sub claim (which is app-specific).

1. Navigate to **Identity Providers** > select your **Entra ID provider**.
2. Go to the **Mappers tab**.
3. Click **Add Mapper**.
4. Fill in the mapper details:
   - **Name**: map_oid_as_brokerid (or any descriptive name)
   - **Sync Mode**: Force
   - **Mapper Type**: Username Template Importer
   - **Template**: ${CLAIM.oid}
   - **Target**: BROKER_ID
5. Click **Save**.

This mapper tells Keycloak to use the Entra oid claim as the Broker ID, ensuring that the login user is matched correctly with the SCIM-provisioned user.

**Step 4: Enable Identity Provider Linking in SCIM**

Finally, instruct your SCIM server to automatically link users to the configured Identity Provider during provisioning:

Add the following attribute to your SCIM configuration (only supported by organization server currently): 

    SCIM_LINK_IDP=true

This will ensure that when a user is provisioned via SCIM, a corresponding Identity Provider link is also created automatically based on the externalId / oid.

## License

[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
  
---

<div id="metatavu-custom-footer"><div align="center">
    <img src="data:image/jpeg;base64,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" alt="Organization Logo" width="100">
    <p>© 2025 ARE Event Productions. All rights reserved.</p>
    <p>
        <a href="https://www.areep.com">Website</a> | 
        <a href="https://www.facebook.com/are.event.productions/">Facebook</a>
    </p>
</div></div>
