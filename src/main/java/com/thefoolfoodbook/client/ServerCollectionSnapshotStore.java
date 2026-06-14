package com.thefoolfoodbook.client;

import com.thefoolfoodbook.common.book.ServerCollectionSnapshot;
import com.thefoolfoodbook.common.book.ServerCollectionSnapshotContext;

public final class ServerCollectionSnapshotStore {
    private ServerCollectionSnapshotStore() {
    }

    public static ServerCollectionSnapshot snapshot() {
        return ServerCollectionSnapshotContext.snapshot();
    }

    public static boolean hasSnapshot() {
        return !ServerCollectionSnapshotContext.snapshot().isEmpty();
    }

    public static void update(ServerCollectionSnapshot nextSnapshot) {
        ServerCollectionSnapshotContext.update(nextSnapshot);
    }

    public static void clear() {
        ServerCollectionSnapshotContext.clear();
    }
}
