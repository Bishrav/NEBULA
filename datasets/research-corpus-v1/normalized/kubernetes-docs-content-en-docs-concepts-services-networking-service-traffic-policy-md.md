_Service Internal Traffic Policy_ enables internal traffic restrictions to only route
internal traffic to endpoints within the node the traffic originated from. The
"internal" traffic here refers to traffic originated from Pods in the current
cluster. This can help to reduce costs and improve performance.

## Using Service Internal Traffic Policy

You can enable the internal-only traffic policy for a
`.spec.internalTrafficPolicy` to `Local`. This tells kube-proxy to only use node local
endpoints for cluster internal traffic.

For pods on nodes with no endpoints for a given Service, the Service
behaves as if it has zero endpoints (for Pods on this node) even if the service
does have endpoints on other nodes.

The following example shows what a Service looks like when you set
`.spec.internalTrafficPolicy` to `Local`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  selector:
    app.kubernetes.io/name: MyApp
  ports:
    - protocol: TCP
      port: 80
      targetPort: 9376
  internalTrafficPolicy: Local
```

## How it works

The kube-proxy filters the endpoints it routes to based on the
`spec.internalTrafficPolicy` setting. When it's set to `Local`, only node local
endpoints are considered. When it's `Cluster` (the default), or is not set,
Kubernetes considers all endpoints.

## {{% heading "whatsnext" %}}

* Read about [Topology Aware Routing](/docs/concepts/services-networking/topology-aware-routing)
* Read about [Service External Traffic Policy](/docs/tasks/access-application-cluster/create-external-load-balancer/#preserving-the-client-source-ip)
* Follow the [Connecting Applications with Services](/docs/tutorials/services/connect-applications-service/) tutorial
