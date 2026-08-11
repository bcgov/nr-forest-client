# Sectigo Public Server Authentication Root R46

This directory contains a **public** root CA certificate, checked into source control
per Option A of the Sectigo R46 CA build remediation plan. It is not a credential:
it contains no private key and grants no access by itself. It is required so the
GraalVM native-image build trusts Canada Post's TLS chain, which was rotated to this
root.

## Certificate record

| Field | Value |
|---|---|
| File | `sectigo-r46-root.pem` |
| Subject | `CN=Sectigo Public Server Authentication Root R46, O=Sectigo Limited, C=GB` |
| Issuer | `CN=Sectigo Public Server Authentication Root R46, O=Sectigo Limited, C=GB` (self-signed root) |
| Serial number | `75:8d:fd:8b:ae:7c:07:00:fa:a9:25:a7:e1:c7:ad:14` |
| Not before | 2021-03-22T00:00:00Z |
| Not after | 2046-03-21T23:59:59Z |
| SHA-256 fingerprint (pinned) | `7B:B6:47:A6:2A:EE:AC:88:BF:25:7A:A5:22:D0:1F:FE:A3:95:E0:AB:45:C7:3F:93:F6:56:54:EC:38:F2:5A:06` |
| Import alias in `cacerts` | `sectigo-r46-root` |
| Type | Root CA (`BasicConstraints: CA=true`), no private key present |

The pinned fingerprint above is also encoded as the `SECTIGO_R46_SHA256` build
argument default in [`backend/Dockerfile`](../Dockerfile) and is verified there
before the certificate is imported. A build fails if the checked-in PEM does not
hash to this value.

## Provenance

- Source: extracted from the Mozilla-derived CA bundle published at
  `https://curl.se/ca/cacert.pem` (curl's official CA bundle), which lists this
  certificate under the entry `Sectigo Public Server Authentication Root R46`.
- Not sourced from `crt.sh` or any other transient/CI-time lookup.
- Verified independently with `X509Certificate2` parsing (subject, issuer, serial,
  validity, SHA-256 fingerprint) before being checked in.

> **Action required before merge:** this record was captured and formatted by an
> automated coding assistant, not by the certificate/security owner named in
> Phase 0 of the remediation plan. Before merging, the platform/security owner
> must independently re-verify the fingerprint above (e.g. with
> `openssl x509 -in sectigo-r46-root.pem -noout -sha256 -fingerprint`) against an
> authoritative Sectigo source, sign off, and record their name/date here.

## Rotation

If Canada Post or Sectigo rotates this root:

1. Obtain the new certificate from an authoritative source (Sectigo's certificate
   repository or a verified endpoint capture) — not from an implicit build-time
   fetch.
2. Validate it locally (`openssl x509 -noout -subject -issuer -serial -dates
   -fingerprint -sha256`).
3. Replace `sectigo-r46-root.pem` and update the `SECTIGO_R46_SHA256` default in
   `backend/Dockerfile` and the table above in the same pull request, so the
   certificate and its pinned fingerprint change atomically.
4. Have the change reviewed as a security-sensitive dependency update.
