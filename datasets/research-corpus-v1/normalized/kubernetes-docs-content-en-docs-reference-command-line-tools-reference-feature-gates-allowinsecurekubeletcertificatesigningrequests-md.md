Disable node admission validation of
[CertificateSigningRequests](/docs/reference/access-authn-authz/certificate-signing-requests/#certificate-signing-requests)
for kubelet signers. Unless you disable this feature gate, Kubernetes enforces that new
kubelet certificates have a `commonName` matching `system:node:$nodeName`.
