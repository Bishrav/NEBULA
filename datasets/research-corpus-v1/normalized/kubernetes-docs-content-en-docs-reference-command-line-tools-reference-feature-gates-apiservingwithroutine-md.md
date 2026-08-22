This feature gate enables an API server performance improvement:
the API server can use separate goroutines (lightweight threads managed by the Go runtime)
to serve [**watch**](/docs/reference/using-api/api-concepts/#efficient-detection-of-changes)
requests.
