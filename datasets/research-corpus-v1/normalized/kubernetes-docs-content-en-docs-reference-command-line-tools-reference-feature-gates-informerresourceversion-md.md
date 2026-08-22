Allow clients to use the `LastSyncResourceVersion()` call on informers, enabling
them to perform actions based on the current resource version. When disabled,
`LastSyncResourceVersion()` succeeds but returns an empty string. Used by
kube-controller-manager for StorageVersionMigration.
