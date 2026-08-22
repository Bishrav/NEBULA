Enables scheduler [queueing hints](/docs/concepts/scheduling-eviction/scheduling-framework/#queueinghint),
which benefits to reduce the useless requeuing.
The scheduler retries scheduling pods if something changes in the cluster that could make the pod scheduled.
Queueing hints are internal signals that allow the scheduler to filter the changes in the cluster
that are relevant to the unscheduled pod, based on previous scheduling attempts.
