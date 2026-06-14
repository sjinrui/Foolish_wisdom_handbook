package com.thefoolfoodbook.client;

import com.thefoolfoodbook.common.book.CollectionEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class SearchMatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchMatcher.class);
    private static final Map<Character, String> FALLBACK_PINYIN = fallbackPinyin();
    private static Object pinIn;
    private static Method containsMethod;
    private static boolean pinInLoadAttempted;

    private SearchMatcher() {
    }

    public static boolean matches(CollectionEntry entry, String query) {
        return matches(entry.id(), entry.displayName(), query);
    }

    public static boolean matches(String id, String displayName, String query) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isBlank()) {
            return true;
        }

        String normalizedId = normalize(id);
        String normalizedDisplayName = normalize(displayName);
        if (containsAllWords(normalizedId, normalizedQuery) || containsAllWords(normalizedDisplayName, normalizedQuery)) {
            return true;
        }
        if (pinInContains(displayName, query)) {
            return true;
        }

        String pinyin = fallbackPinyin(displayName, false);
        String initials = fallbackPinyin(displayName, true);
        return containsAllWords(pinyin, normalizedQuery) || containsAllWords(initials, normalizedQuery);
    }

    private static boolean containsAllWords(String text, String query) {
        if (text.contains(query)) {
            return true;
        }
        for (String word : query.split(" ")) {
            if (!word.isBlank() && !text.contains(word)) {
                return false;
            }
        }
        return true;
    }

    private static boolean pinInContains(String text, String query) {
        try {
            if (!loadPinIn()) {
                return false;
            }
            return Boolean.TRUE.equals(containsMethod.invoke(pinIn, text, query));
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ex) {
            LOGGER.debug("Could not match query with JECharacters PinIn", ex);
            return false;
        }
    }

    private static boolean loadPinIn() {
        if (pinInLoadAttempted) {
            return pinIn != null && containsMethod != null;
        }

        pinInLoadAttempted = true;
        try {
            Class<?> pinInClass = Class.forName("me.towdium.pinin.PinIn");
            pinIn = pinInClass.getConstructor().newInstance();
            containsMethod = pinInClass.getMethod("contains", String.class, String.class);
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.debug("JECharacters PinIn is not available", ex);
        }
        return pinIn != null && containsMethod != null;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace(':', ' ')
                .replace('/', ' ')
                .trim()
                .replaceAll("\\s+", " ");
    }

    private static String fallbackPinyin(String text, boolean initialsOnly) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            String pinyin = FALLBACK_PINYIN.get(text.charAt(i));
            if (pinyin == null) {
                continue;
            }
            result.append(initialsOnly ? pinyin.charAt(0) : pinyin);
        }
        return result.toString();
    }

    private static Map<Character, String> fallbackPinyin() {
        Map<Character, String> map = new HashMap<>();
        map.put('\u5e73', "ping");
        map.put('\u539f', "yuan");
        map.put('\u68ee', "sen");
        map.put('\u6797', "lin");
        map.put('\u6c99', "sha");
        map.put('\u6f20', "mo");
        map.put('\u7af9', "zhu");
        map.put('\u4e1b', "cong");
        map.put('\u70ed', "re");
        map.put('\u5e26', "dai");
        map.put('\u8349', "cao");
        map.put('\u5730', "di");
        map.put('\u9ad8', "gao");
        map.put('\u539f', "yuan");
        map.put('\u6076', "e");
        map.put('\u51b0', "bing");
        map.put('\u51bb', "dong");
        map.put('\u6df1', "shen");
        map.put('\u6d77', "hai");
        map.put('\u6d0b', "yang");
        map.put('\u5c71', "shan");
        map.put('\u5cf0', "feng");
        map.put('\u6cb3', "he");
        map.put('\u96ea', "xue");
        map.put('\u6cbc', "zhao");
        map.put('\u6cfd', "ze");
        map.put('\u7ea2', "hong");
        map.put('\u6811', "shu");
        map.put('\u9ed1', "hei");
        map.put('\u6697', "an");
        map.put('\u6a31', "ying");
        map.put('\u82b1', "hua");
        map.put('\u7e41', "fan");
        map.put('\u8302', "mao");
        map.put('\u6d1e', "dong");
        map.put('\u7a74', "xue");
        map.put('\u66ae', "mu");
        map.put('\u8272', "se");
        map.put('\u5e7d', "you");
        map.put('\u51a5', "ming");
        map.put('\u5929', "tian");
        map.put('\u5883', "jing");
        map.put('\u6781', "ji");
        map.put('\u4e1c', "dong");
        map.put('\u5357', "nan");
        map.put('\u897f', "xi");
        map.put('\u5317', "bei");
        map.put('\u865a', "xu");
        map.put('\u7a7a', "kong");
        return Map.copyOf(map);
    }
}
