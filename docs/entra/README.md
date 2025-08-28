# Azure Entra ID SCIM Configuration

This extension is compatible with **Microsoft Entra ID** SCIM provisioning.
### Keycloak Configuration

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

### Azure Configuration

Step-by-step guide for Azure:

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

### Identity Provider Linking with Azure Entra ID

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