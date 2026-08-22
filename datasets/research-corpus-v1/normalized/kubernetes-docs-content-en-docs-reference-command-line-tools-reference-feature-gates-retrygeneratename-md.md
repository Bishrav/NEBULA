Enables retrying of object creation when the
is expected to generate a [name](/docs/concepts/overview/working-with-objects/names/#names).

When this feature is enabled, requests using `generateName` are retried automatically in case the
control plane detects a name conflict with an existing object, up to a limit of 8 total attempts.
