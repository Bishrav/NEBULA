When enabled, kube-scheduler uses `.status.nominatedNodeName` to express where a
Pod is going to be bound. The `.status.nominatedNodeName` field is set when kube-scheduler
triggers preemption of pods, or anticipates that WaitOnPermit or PreBinding phase will take
relatively long.
Other components may read and use `.status.nominatedNodeName`, but should not set it.

When disabled, kube-scheduler will only set `.status.nominatedNodeName` before triggering preemption.
