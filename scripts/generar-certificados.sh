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
