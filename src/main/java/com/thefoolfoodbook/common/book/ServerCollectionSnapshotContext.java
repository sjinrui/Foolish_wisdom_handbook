package com.thefoolfoodbook.common.book;

public final class ServerCollectionSnapshotContext {
    private static ServerCollectionSnapshot snapshot = ServerCollectionSnapshot.empty();

    private ServerCollectionSnapshotContext() {
    }

    public static ServerCollectionSnapshot snapshot() {
        return snapshot;
    }

    public static void update(ServerCollectionSnapshot nextSnapshot) {
        snapshot = nextSnapshot == null ? ServerCollectionSnapshot.empty() : nextSnapshot;
    }

    public static void clear() {
        snapshot = ServerCollectionSnapshot.empty();
    }
}
