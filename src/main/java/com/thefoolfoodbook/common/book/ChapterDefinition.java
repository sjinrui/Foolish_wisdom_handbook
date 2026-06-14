package com.thefoolfoodbook.common.book;

import java.util.function.Supplier;

public record ChapterDefinition(String id, String title, Supplier<CollectionChapter> loader) {
}
