Enables the `kube-controller-manager` to actively release its leader election lock
during leader transitions, rather than waiting for the lock's TTL to expire.
This allows a new leader to be elected more quickly.
