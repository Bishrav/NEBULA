You can enable the `MultiCIDRServiceAllocator` feature gate. The API server supports migration
from the old bitmap ClusterIP allocators to the new IPAddress allocators.

The API server performs a dual-write on both allocators. This feature gate disables the dual write
on the new Cluster IP allocators; you can enable this feature gate if you have completed the
relevant stage of the migration.
