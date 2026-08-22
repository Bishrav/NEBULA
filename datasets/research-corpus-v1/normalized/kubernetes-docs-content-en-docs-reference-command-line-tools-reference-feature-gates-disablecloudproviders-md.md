Enabling this feature gate deactivated functionality in `kube-apiserver`,
`kube-controller-manager` and `kubelet` that related to the `--cloud-provider`
command line argument.

In Kubernetes v1.31 and later, the only valid values for `--cloud-provider`
are the empty string (no cloud provider integration), or "external"
(integration via a separate cloud-controller-manager).
