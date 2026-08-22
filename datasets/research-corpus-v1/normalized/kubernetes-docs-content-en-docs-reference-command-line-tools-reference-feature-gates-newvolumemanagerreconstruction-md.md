Enables improved discovery of mounted volumes during kubelet
startup. Since the associated code had been significantly refactored, Kubernetes versions 1.25 to 1.29
allowed you to opt-out in case the kubelet got stuck at the startup, or did not unmount volumes
from terminated Pods.

This refactoring was behind the `SELinuxMountReadWriteOncePod`  feature gate in Kubernetes
releases 1.25 and 1.26.
