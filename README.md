# NeteaseModAdder

一个用于在网易 Minecraft 中加载第三方模组的模组，以达到永久装模组的目的。

## 使用方法
0. **准备工作**  
   - 显示文件扩展名：为了方便之后的描述和操作，需要打开显示文件扩展名
     * Windows 7： https://jingyan.baidu.com/article/9080802281e294fd91c80fe4.html
     * Windows 10：https://jingyan.baidu.com/article/f00622282564bdfbd3f0c827.html
   - 下载 NeteaseModAdder： https://github.com/ZekerZhayard/NeteaseModAdder/releases
   - 下载自己想安装的第三方模组
1. **找到 MCLDownload 文件夹**：网易 Minecraft 本体位于 `MCLDownload` 文件夹内
   - 打开注册表编辑器：https://jingyan.baidu.com/article/6181c3e0b33976152ef153c7.html
   - 在注册表编辑器内找到路径 `HKEY_CURRENT_USER\Software\Netease\MCLauncher`
   - 名称为 `DownloadPath` 对应的数据就是 `MCLDownload` 文件夹的路径
2. **修改 jre 版本**：目前不少模组都需要 jre8 ，由于网易 Minecraft 启动部分游戏使用的是 jre7 ，所以需要将网易 Minecraft 自带的 jre7 修改为 jre8
   - 查看自己的操作系统是32位的还是64位的：https://jingyan.baidu.com/article/27fa73265ed13046f8271f19.html
   - 如果是32位操作系统：
     * 下载 https://x19.gdl.netease.com/jre-v32.7z
     * 找到路径 `MCLDownload\ext\jre-v32`
   - 如果是64位操作系统：
     * 下载 https://x19.gdl.netease.com/jre-v64-170307.7z
     * 找到路径 `MCLDownload\ext\jre-v64-170307`
   - 如果找不到上述路径，那么需要先启动一次网易 Minecraft ，之后会自动生成
   - 将上述路径内的 `jre7` 和 `jre8` 两个文件夹删除
   - 把刚刚下载到的文件用压缩软件打开，并将其中的 `jre8` 文件夹解压到上述路径下
   - 在上述路径下新建一个文件夹，并命名为 `jre7`
   - 把 `jre8` 文件夹内的所有文件复制到 `jre7` 文件夹中就行了
3. **重命名本模组**：想了解为什么要按照下面的方法操作，请参考「模组原理」部分
   - 删除 `MCLDownload\cache\component` 文件夹
   - 在启动器组件中心里随便下载一个组件（比如「Wisdom光影 3.2」）
   - 下载完成后， `MCLDownload\cache\component` 又会生成，并在其中有一个数字文件夹（比如「`76002695265125376@4@18`」）
   - 把 NeteaseModAdder 模组文件名重命名为这个数字文件夹的名称（比如「`76002695265125376@4@18.jar`」）
4. **找到网络游戏对应文件夹**：如果你不玩网络游戏，仅玩单人或多人联机或租赁服，那么这一步可以跳过
   - 找到路径 `MCLDownload\Game\<你的邮箱>\NetGame\<你想加模组的网络游戏名称>` （比如「`MCLDownload\Game\abcdefg@163.com\NetGame\Hypixel`」）
   - 用记事本或其他代码编辑器打开其中的`config`文件（比如「`hypixel.config`」）
   - 找到其中 `GUID` 这一项，记录后面那一串数字（比如对于 Hypixel 就应当是 `"GUID": "76826543757722624"`）
   - 找到路径 `MCLDownload\cache\game` ，里面应当存在一个以上一步记录的数字为名称的文件夹，如果不存在，需要启动一次对应的网络游戏，之后会自动生成
5. **复制并修改第三方模组扩展名**
   - 如果你玩的是单人或多人联机或租赁服：
     * 找到路径 `MCLDownload\cache\game\<版本>` （比如「`MCLDownload\cache\game\V_1_12_2`」，如果没有对应版本的文件夹，需要启动一次这个版本的游戏，之后会自动生成）
     * 在其中新建一个文件夹并命名为 `mods` （如果没有`mods`文件夹的话）
   - 如果你玩的是网络游戏：
     * 找到路径 `MCLDownload\cache\game\<GUID>\.minecraft\mods` （如果没有`mods`文件夹就新建一个）
   - 把 NeteaseModAdder 模组（即`76002695265125376@4@18.jar`）和你想装的第三方模组复制到`mods`文件夹中去
   - 把其他第三方模组的文件扩展名由`jar`全部改为`zip`即可
6. **启动游戏**


## 模组原理
本模组利用了网易 Minecraft 启动器两个缺陷。在启动器启动 Minecraft 前，清空 `MCLDownload\Game\.minecraft\mods` 文件夹后，启动器会把 `MCLDownload\cache\game\<GUID>\.minecraft\mods` 或 `MCLDownload\cache\game\V_1_*_*\mods` 中的所有文件复制到 `MCLDownload\Game\.minecraft\mods` 文件夹内，随后**只**删除不符合验证且扩展名为 `jar` 的文件。
- 由于「只删除`jar`文件」，因此把模组扩展名改成`zip`即可。由于网易forge1.7.10\~1.11.2支持加载以`zip`为扩展名的普通模组，网易forge1.12.2支持加载以`zip`为扩展名的普通模组和核心模组，所以本模组的作用是代替forge加载扩展名为`zip`的核心模组或1.13.2\~1.14.3的模组。
- 对于`jar`文件的验证的具体过程不是非常清楚，只知道以下内容：每个组件中心的组件都有唯一的ID，这个ID就是在 `MCLDownload\cache\component` 文件夹内看到的那些，如果有检测到模组名称与当前登录的账号购买过组件中心的组件的ID匹配，那么就不会被删除。

之所以要删除 `MCLDownload\cache\component` 文件夹，是因为下载的组件我认为刚刚下载的组件混在文件夹中很难找到，那么删除之后再下载，文件夹中仅有刚刚下载到的唯一一个组件，就免去了寻找的麻烦，当然有其他方法找到也是可以的（比如根据修改时间之类的）。