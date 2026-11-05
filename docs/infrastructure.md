# KanuControl Infrastructure

## Overview

KanuControl uses a multi-VM architecture with an edge reverse proxy, internal backend services, Keycloak authentication and schema-based PostgreSQL multitenancy.

The infrastructure is designed for:
- secure external HTTPS access
- separation of responsibilities
- internal-only backend communication
- tenant-aware database isolation
- reproducible deployments

---

# Network Architecture

Internet  
↓  
Cloudflare DNS  
↓  
edge-prod (nginx reverse proxy)  
↓  

------------------------------------------------

|                                              |

|                                              |

kcserver-prod                           keycloak-prod  
(Spring Boot API)                      (Keycloak IdP)

---

# Virtual Machines

## edge-prod

### Purpose

- public entrypoint
- HTTPS termination
- reverse proxy
- frontend hosting
- Cloudflare Dynamic DNS

### Responsibilities

- nginx reverse proxy
- Let's Encrypt certificates
- frontend static hosting
- proxying requests to internal services

### Public domains

- kc.kanucontrol.de
- auth.kanucontrol.de

### Internal IP

- 192.168.100.34

### Services

- nginx
- certbot
- cloudflare-ddns cronjob

### Frontend deployment target

```text
/var/www/kc_client/dist
```

## kcserver-prod

Purpose

* Spring Boot backend API
* business logic
* PostgreSQL access
* multitenancy handling

Internal only

* not directly exposed to the internet

Internal IP

* 192.168.100.32

### Ports
8090

### Technology

* Spring Boot 3
* Hibernate
* Liquibase
* PostgreSQL

### Responsibilities

* REST API
* tenant resolution
* schema switching
* Liquibase schema migrations
* participant management
* billing logic
* planning logic

### Booking status email (SMTP)

Status emails selected by a user in KanuControl are sent by the backend after the booking status has been committed. Configure SMTP on the backend service with environment variables; do not commit credentials to this repository:

| Variable | Purpose | Default |
|---|---|---|
| `KC_SMTP_ENABLED` | Enable direct status email delivery | `false` |
| `KC_SMTP_TEST_MODE` | Redirect SMTP status emails exclusively to the test recipient, with `[TEST]` subject prefix | `false` |
| `KC_SMTP_TEST_RECIPIENT` | Single plain email address required in test mode | — |
| `KC_SMTP_HOST` | SMTP host | — |
| `KC_SMTP_PORT` | SMTP port | `587` |
| `KC_SMTP_USERNAME` | SMTP account name | — |
| `KC_SMTP_PASSWORD` | SMTP account password | — |
| `KC_SMTP_FROM` | Authorized sender address | — |
| `KC_SMTP_STARTTLS` | Use STARTTLS (typical port 587) | `true` |
| `KC_SMTP_SSL` | Use implicit TLS (typical port 465) | `false` |

Set `KC_SMTP_STARTTLS=false` when using implicit TLS. When SMTP is enabled, host and sender address are required; username and password must either both be present or both be empty. The backend reports a send error to the user while preserving the already changed booking status. Website booking intake confirmation emails continue to use the WordPress mail service.

For booking tests, set `KC_SMTP_TEST_MODE=true` and `KC_SMTP_TEST_RECIPIENT=your-test-address@example.com` alongside the existing SMTP settings and restart the updated backend. With SMTP enabled, an invalid or missing test address prevents startup. Delivery checks it again and never falls back to the tenant address. With test mode disabled, the original recipient is used. These settings apply only to backend SMTP status emails, not WordPress intake confirmations or the legacy status-mail queue.

## keycloak-prod

### Purpose

* authentication and authorization
* OpenID Connect provider

### Internal only

* exposed externally only through edge-prod

### Responsibilities

* login
* token generation
* tenant group mapping
* realm management

### Public endpoint
https://auth.kanucontrol.de

## Reverse Proxy Setup

kc.kanucontrol.de

### Proxied by

* edge-prod nginx

### Target
http://192.168.100.34:8090

## auth.kanucontrol.de

Proxied by

* edge-prod nginx

Target

* internal Keycloak instance

Purpose

* Keycloak authentication
* OIDC endpoints
* admin console

## HTTPS

HTTPS certificates are managed by:

* certbot
* Let’s Encrypt

Certificates are terminated on:

* edge-prod

Automatic renewal:

* systemd timer / certbot renewal

## Cloudflare

Cloudflare is used for:

* DNS management
* Dynamic DNS updates

### Current mode
DNS only (grey cloud)

### Reason

* direct Let’s Encrypt compatibility
* easier debugging
* no Cloudflare SSL proxy complications

### Dynamic IP updates

* handled via Cloudflare API token
* cronjob on edge-prod

## PostgreSQL Multitenancy

## Strategy

### KanuControl uses:

* schema-per-tenant multitenancy

Each tenant has its own PostgreSQL schema.

Examples

* kanu
* ekc_eschweilerkanuclub
* okc_oberhausen
* svbt_boichthum

## Tenant Resolution

Tenant resolution happens via:

* JWT token
* Keycloak groups

### Flow

1. User logs in via Keycloak
2. JWT contains tenant/group
3. JwtTenantFilter extracts tenant
4. TenantContext is populated
5. Hibernate switches schema dynamically

## Liquibase Strategy

Master Schema

Schema:
kanu

acts as:

* reference schema
* Liquibase master schema


Existing Tenant Schemas

Older tenant schemas were originally created by:

* copying tables from kanu

To migrate these schemas into Liquibase governance:

* TenantLiquibaseMigrator uses changeLogSync()

This allows:

* keeping existing data
* avoiding schema recreation
* enabling future migrations

⸻

Startup Migration

On application startup:

1. all managed schemas are discovered
2. Liquibase migrations are executed
3. existing schemas without DATABASECHANGELOG are bootstrapped
4. future changesets are applied normally

⸻

## Frontend Deployment

### Frontend Technology

* React
* Vite
* TypeScript

### Deployment target

* edge-prod

### Build process

1. yarn build
2. copy dist/ to edge-prod
3. nginx reload

### Static hosting path
/var/www/kc_client/dist


## Security Notes

* backend VMs are internal only
* HTTPS enforced externally
* DNS rebinding exceptions configured in FritzBox
* Cloudflare used only for DNS
* tenant isolation via PostgreSQL schemas
* Keycloak handles authentication centrally

⸻

### Operational Notes

#### Important locations

nginx config
/etc/nginx/sites-enabled/

#### Let’s Encrypt
/var/www/kc_client/dist

#### Frontend
/var/www/kc_client/dist

#### Liquibase
db/changelog/

## Future Improvements

Potential future improvements:

* Infrastructure as Code
* Docker Compose
* automated deployments
* dedicated Liquibase migration user
* centralized logging
* backup automation
* monitoring
* CI/CD pipeline
