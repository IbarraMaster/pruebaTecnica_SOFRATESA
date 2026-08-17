#!/usr/bin/env bash
set -e
mkdir -p proxy/certs
export MSYS_NO_PATHCONV=1
openssl req -x509 -nodes -newkey rsa:2048 \
  -keyout proxy/certs/sofratesa.key \
  -out proxy/certs/sofratesa.crt \
  -days 365 \
  -subj "/CN=sofratesa.local" \
  -addext "subjectAltName=DNS:sofratesa.local,DNS:localhost,IP:127.0.0.1,IP:10.0.2.2"
echo "OK: proxy/certs/"

# La app Android (variante debug) necesita el certificado embebido como
# trust-anchor para confiar en el proxy vía network_security_config.xml.
# No se versiona (mismo certificado generado arriba, ver .gitignore).
mkdir -p android/app/src/debug/res/raw
cp proxy/certs/sofratesa.crt android/app/src/debug/res/raw/sofratesa_ca.pem
echo "OK: android/app/src/debug/res/raw/sofratesa_ca.pem"
