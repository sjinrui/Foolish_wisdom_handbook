# Foolish Wisdom Handbook

大愚若智之书是一个用于愚者整合包的模组。它提供一本游戏内收集手册，用于查看各个流派的收集信息。

## 功能

- 按流派（美食、变形、武器大师、旅人、唱片、猫狐等）查看已收集 / 未收集的物品。
- **新收集提醒**：每次打开手册时，若发现新收集的条目，会以金色高亮 + 右上角 NEW 角标标出，并在顶部出现“新 N”按钮；点击该按钮确认并清除高亮。即使在“未收集”显示模式下，新收集的条目也会临时保留在“新收集”分组中，确认后才正常隐藏。
- **按获取时间排序**：顶部排序按钮可在“默认 / 最新在前 / 最早在前”之间循环切换。
- 收集时间记录在客户端、按世界（单人世界名 / 多人服务器地址）隔离，保存在游戏目录 `foolish_wisdom/<worldKey>.json`。

## 环境

- Minecraft：1.20.1
- Forge：47.4.0
- Java 工具链：17
- 模组 ID：`foolish_wisdom_handbook`
- 当前版本：`1.1`

## 构建

请在项目根目录使用 Gradle Wrapper：

```powershell
.\gradlew.bat check
.\gradlew.bat build
```

在 Linux 或 macOS 上：

```sh
./gradlew check
./gradlew build
```

## 构建产物

构建完成后，模组 jar 会生成在：

```text
build/libs/foolish_wisdom_handbook-<version>.jar
```

对于版本 `1.1`，预期产物名称为：

```text
build/libs/foolish_wisdom_handbook-1.1.jar
```

Git 会忽略构建输出、Gradle 缓存、运行目录、日志和本地 IDE 文件。

## 许可证

本项目使用 GNU General Public License v3.0 or later（GPL-3.0-or-later）开源。

允许使用、修改、整合、移植和再发布本项目；分发修改版或二进制版本时，必须按 GPL 提供对应源码并继续以 GPL 兼容方式发布。完整条款见 [LICENSE](LICENSE)。

## English

Foolish Wisdom Handbook is a Minecraft Forge 1.20.1 mod for The Fool modpack. It provides an in-game handbook for collection and progress information.

## Environment

- Minecraft: 1.20.1
- Forge: 47.4.0
- Java toolchain: 17
- Mod id: `foolish_wisdom_handbook`
- Current version: `1.1`

## Build

Use the Gradle Wrapper from the project root:

```powershell
.\gradlew.bat check
.\gradlew.bat build
```

On Linux or macOS:

```sh
./gradlew check
./gradlew build
```

## Output

The built mod jar is generated under:

```text
build/libs/foolish_wisdom_handbook-<version>.jar
```

For version `1.1`, the expected artifact name is:

```text
build/libs/foolish_wisdom_handbook-1.1.jar
```

Build outputs, Gradle caches, run directories, logs, and local IDE files are intentionally ignored by Git.

## License

This project is licensed under the GNU General Public License v3.0 or later (GPL-3.0-or-later).

You may use, modify, integrate, port, and redistribute this project. When distributing modified or binary versions, you must provide the corresponding source code and keep the work under GPL-compatible terms. See [LICENSE](LICENSE) for details.
