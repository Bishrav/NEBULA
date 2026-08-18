# Distributed Search Architecture

The query coordinator sends a search request to multiple shards. Each shard returns local top-k results, and the coordinator merges them into one globally ranked response.
