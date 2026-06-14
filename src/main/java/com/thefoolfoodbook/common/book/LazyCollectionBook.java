package com.thefoolfoodbook.common.book;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LazyCollectionBook {
    private final List<ChapterDefinition> definitions;
    private final Map<String, CollectionChapter> loadedChapters = new LinkedHashMap<>();

    public LazyCollectionBook(List<ChapterDefinition> definitions) {
        this.definitions = List.copyOf(definitions);
    }

    public List<ChapterDefinition> definitions() {
        return definitions;
    }

    public boolean isLoaded(String chapterId) {
        return loadedChapters.containsKey(chapterId);
    }

    public CollectionChapter load(String chapterId) {
        CollectionChapter loaded = loadedChapters.get(chapterId);
        if (loaded != null) {
            return loaded;
        }

        ChapterDefinition definition = definitionById(chapterId);
        if (definition == null) {
            return CollectionChapter.empty(chapterId, "", false);
        }

        CollectionChapter chapter = definition.loader().get();
        loadedChapters.put(chapterId, chapter);
        return chapter;
    }

    public CollectionChapter chapterOrPlaceholder(String chapterId) {
        CollectionChapter loaded = loadedChapters.get(chapterId);
        if (loaded != null) {
            return loaded;
        }

        ChapterDefinition definition = definitionById(chapterId);
        if (definition == null) {
            return CollectionChapter.empty(chapterId, "", false);
        }
        return CollectionChapter.empty(definition.id(), definition.title(), true);
    }

    public void invalidate(String chapterId) {
        loadedChapters.remove(chapterId);
    }

    public String firstChapterId() {
        return definitions.isEmpty() ? "" : definitions.get(0).id();
    }

    private ChapterDefinition definitionById(String chapterId) {
        for (ChapterDefinition definition : definitions) {
            if (definition.id().equals(chapterId)) {
                return definition;
            }
        }
        return null;
    }
}
