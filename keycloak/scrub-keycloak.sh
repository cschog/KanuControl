#!/usr/bin/env bash
set -euo pipefail

INPUT=${1:-KanuControl-realm.json}
OUTPUT=${2:-KanuControl-realm-public.json}

echo "Scrubbing Keycloak export..."
echo "Input : $INPUT"
echo "Output: $OUTPUT"

jq '
  # Remove client secrets
  (.clients[]? |= (
      del(.secret)
      | del(.clientAuthenticatorConfig.clientSecret)
  ))

  # Remove user credentials + IdP links
  | (.users[]? |= (
      del(.credentials)
      | del(.federatedIdentities)
  ))

  # Remove Identity Provider secrets
  | (.identityProviders[]? |= (
      del(.config.clientSecret)
      | del(.config.clientAssertionSigningKeyPassword)
  ))

  # Remove SMTP password
  | (.smtpServer.password? = "")

  # Remove realm private keys
  | del(.components."org.keycloak.keys.KeyProvider")

  # OPTIONAL: remove all users completely (for public repo)
  | del(.users)

' "$INPUT" > "$OUTPUT"

echo "✔ Scrub finished"