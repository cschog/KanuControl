# KanuControl Deployment Guide

## Overview

KanuControl uses a separated deployment architecture:

| Component | Target VM |
|---|---|
| Frontend (React/Vite) | edge-prod |
| Backend (Spring Boot) | kcserver-prod |
| Authentication (Keycloak) | keycloak-prod |

The edge VM acts as the public HTTPS reverse proxy.

---

# Domains

| Domain | Purpose |
|---|---|
| kc.kanucontrol.de | Frontend + API |
| auth.kanucontrol.de | Keycloak |

---

# Frontend Deployment

## Target

Frontend is deployed to:

```text
edge-prod:/var/www/kc_client/dist