package com.thefoolfoodbook.common.book;

public enum CollectionDisplayMode {
    MISSING("\u672a\u6536\u96c6"),
    ALL("\u5168\u90e8"),
    COLLECTED("\u5df2\u6536\u96c6");

    private final String label;

    CollectionDisplayMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean includes(boolean collected) {
        return this == ALL || (this == COLLECTED) == collected;
    }

    public CollectionDisplayMode next() {
        return switch (this) {
            case MISSING -> ALL;
            case ALL -> COLLECTED;
            case COLLECTED -> MISSING;
        };
    }
}
