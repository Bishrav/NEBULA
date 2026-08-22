When `LocalStorageCapacityIsolation`
is enabled for
[local ephemeral storage](/docs/concepts/configuration/manage-resources-containers/),
the backing filesystem for [emptyDir volumes](/docs/concepts/storage/volumes/#emptydir) supports project quotas,
and `UserNamespacesSupport` is enabled,
project quotas are used to monitor `emptyDir` volume storage consumption rather than using filesystem walk, ensuring better performance and accuracy.
