When disabled, legacy behavior in service load balancers and
node disruption will ignore the `node-role.kubernetes.io/master` label in favor of the
feature-specific labels provided by `NodeDisruptionExclusion` and `ServiceNodeExclusion`.
