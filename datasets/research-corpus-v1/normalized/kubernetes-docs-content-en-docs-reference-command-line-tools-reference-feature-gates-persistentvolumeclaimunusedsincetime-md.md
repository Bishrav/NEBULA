When enabled, the PVC protection controller adds an `Unused` condition to
PersistentVolumeClaims that tracks whether the PVC is currently referenced by
any non-terminal Pod. The condition's `lastTransitionTime` records when the PVC
last transitioned between being in use and being unused.
