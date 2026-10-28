package com.thefoolfoodbook.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户端本地的“收集记忆”：记录每个章节里每个条目首次被发现（已收集）的时间，
 * 以及玩家最后一次确认“新收集提醒”的时间。
 *
 * 数据按当前世界（单人世界名 / 多人服务器地址）隔离，存放在
 * 游戏目录下的 foolish_wisdom/ 文件夹中，使用 JSON 持久化。
 */
public final class CollectionMemoryStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionMemoryStore.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static CollectionMemoryStore active;

    private final Path file;
    private final Data data;

    private CollectionMemoryStore(Path file, Data data) {
        this.file = file;
        this.data = data;
    }

    /** 获取当前世界对应的存储实例（切换世界时自动重新加载）。 */
    public static synchronized CollectionMemoryStore get() {
        Minecraft mc = Minecraft.getInstance();
        Path file = mc.gameDirectory.toPath()
                .resolve("foolish_wisdom")
                .resolve(sanitizeFileName(worldKey(mc)) + ".json");
        if (active == null || !active.file.equals(file)) {
            active = load(file);
        }
        return active;
    }

    private static CollectionMemoryStore load(Path file) {
        Data data = new Data();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                Data parsed = GSON.fromJson(reader, Data.class);
                if (parsed != null) {
                    data = parsed;
                }
            } catch (Exception ex) {
                LOGGER.debug("Could not load collection memory from {}", file, ex);
            }
        }
        if (data.times == null) {
            data.times = new HashMap<>();
        }
        return new CollectionMemoryStore(file, data);
    }

    /**
     * 记录某章节当前已收集的条目列表。
     *
     * 第一次见到该章节时，所有已收集条目都视为“历史收集”，时间记为 0，不会触发新提醒；
     * 该章节已被记录过之后，新出现的已收集条目才会打上当前时间，作为新收集。
     */
    public void recordChapter(String chapterId, List<String> collectedIds) {
        long now = System.currentTimeMillis();
        Map<String, Long> chapterTimes = data.times.get(chapterId);
        boolean chapterKnown = chapterTimes != null;
        if (chapterTimes == null) {
            chapterTimes = new HashMap<>();
            data.times.put(chapterId, chapterTimes);
        }
        boolean changed = false;
        for (String id : collectedIds) {
            if (!chapterTimes.containsKey(id)) {
                chapterTimes.put(id, chapterKnown ? now : 0L);
                changed = true;
            }
        }
        if (changed) {
            save();
        }
    }

    /** 查询某条目的首次收集时间；未收集 / 未知时返回 0。 */
    public long collectTime(String chapterId, String entryId) {
        Map<String, Long> chapterTimes = data.times.get(chapterId);
        if (chapterTimes == null) {
            return 0L;
        }
        Long time = chapterTimes.get(entryId);
        return time == null ? 0L : time;
    }

    /** 判断某条目是否为“尚未确认的新收集”。 */
    public boolean isNew(String chapterId, String entryId) {
        long time = collectTime(chapterId, entryId);
        return time > 0L && time > data.acknowledgedAt;
    }

    /** 统计所有章节中尚未确认的新收集条目总数。 */
    public int newCount() {
        int count = 0;
        for (Map<String, Long> chapterTimes : data.times.values()) {
            for (Long time : chapterTimes.values()) {
                if (time != null && time > 0L && time > data.acknowledgedAt) {
                    count++;
                }
            }
        }
        return count;
    }

    /** 确认当前所有新收集提醒：把确认时间更新为现在，之后不再高亮这些条目。 */
    public void acknowledge() {
        data.acknowledgedAt = System.currentTimeMillis();
        save();
    }

    private void save() {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException ex) {
            LOGGER.debug("Could not save collection memory to {}", file, ex);
        }
    }

    private static String worldKey(Minecraft mc) {
        if (mc.getCurrentServer() != null
                && mc.getCurrentServer().ip != null
                && !mc.getCurrentServer().ip.isBlank()) {
            return "server_" + mc.getCurrentServer().ip;
        }
        if (mc.getSingleplayerServer() != null) {
            return "local_" + mc.getSingleplayerServer().getWorldData().getLevelName();
        }
        if (mc.player != null) {
            return "player_" + mc.player.getUUID();
        }
        return "default";
    }

    private static String sanitizeFileName(String key) {
        return key.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /** JSON 持久化数据。 */
    private static final class Data {
        long acknowledgedAt = 0L;
        Map<String, Map<String, Long>> times = new HashMap<>();
    }
}
