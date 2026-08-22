Configure the `kube-controller-manager` to publish a
to every namespace. This ConfigMap contains a CA bundle used for verifying connections
to the kube-apiserver. See
[Bound Service Account Tokens](https://github.com/kubernetes/enhancements/blob/master/keps/sig-auth/1205-bound-service-account-tokens/README.md)
for more details.
