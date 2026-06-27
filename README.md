# Blockbuster Studio Next

一个基于 Fabric 的 Minecraft 模组，用于在 Minecraft 中制作动画和电影。  
A Minecraft mod for Fabric that allows creating animations and cinematics within Minecraft.

继承自 [McHorse 的 BBS mod](https://github.com/mchorse/bbs)，已迁移至 Minecraft 26.2，由 [lemonungood](https://github.com/lemonungood) 维护。

## 功能 / Features

- 🎬 **角色动画** - 关键帧动画系统，支持路径、旋转、缩放、轨迹等
- 🎭 **形态系统** - 变身成任意实体、模型方块、粒子效果等
- 🎥 **摄像机控制** - 推拉、摇移、轨道、变焦等专业电影级运镜
- 📜 **动作录制** - 录制并回放玩家动作
- 🔊 **音频字幕** - 支持音频剪辑和字幕显示
- 🖼️ **模型方块** - 导入展示自定义模型
- 🔫 **自定义物品** - 枪械等交互物品
- 🌐 **局域网联机** - 支持多人协作制作

## 安装 / Installation

1. 安装 [Fabric Loader](https://fabricmc.net/use/) ≥ 0.19.3
2. 安装 [Fabric API](https://modrinth.com/mod/fabric-api) 对应版本
3. 下载本模组的 JAR 放入 `mods` 文件夹
4. （可选）安装 Sodium / Iris 以获得更好性能

## 构建 / Build

```bash
./gradlew build
```

构建产物在 `build/libs/bbs-next-<version>.jar`。

## 许可证 / License

MIT - 详见 [LICENSE.md](LICENSE.md)

## 链接 / Links

- [Modrinth](https://modrinth.com/mod/bbs-next)
- [GitHub](https://github.com/lemonungood/bbs-next)
- [上游原版 Upstream](https://github.com/mchorse/bbs)
