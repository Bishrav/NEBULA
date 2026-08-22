Change the kube-scheduler to make the entire scheduling cycle free of blocking requests to the Kubernetes API server.
Instead, interact with the Kubernetes API using asynchronous code.
