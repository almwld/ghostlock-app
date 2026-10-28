# LG Wing KernelSU-Next 构建与 boot 镜像审计

**状态：仅供诊断——尚未验证可安全刷写，也未验证可用于 GhostLock 执行。**  
审计日期：2026-10-28

本文汇总在 Termux 与 GitHub Actions 中完成的 LG Wing LM-F100 / SM7250 内核构建和 boot 镜像检查。本文记录证据与未解决事项；不注册可执行的 GhostLock 配置，也不证明手机存在可利用漏洞。

## 构建来源与产物溯源

- 内核源码仓库：[almwld/android_kernel_lge_sm7250](https://github.com/almwld/android_kernel_lge_sm7250)
- 构建分支：`ksunext-winglm-ci`
- 产生本地归档的构建：[Run #13](https://github.com/almwld/android_kernel_lge_sm7250/actions/runs/40608527655)
- Run #13 源码提交：`01a96ec6e19ac2e74b96e6317cf9e563ad8460fa`
- 产物名称：`winglm-kernelsu-next-13`
- Artifact ID：`12783115056`
- ZIP 的 SHA-256：`fff1b40bc5f75d462512ccd79d555b7f7f14fdd95df482aef289357a1ef2cab6`
- Termux 中 `~/downloads/winglm-kernelsu-next.zip` 与 `~/LG_Wing_Root_Project_Archive/winglm-kernelsu-next.zip` 的哈希已分别计算并确认完全一致。
- 审计时 GitHub 显示产物将于 2026-11-11 过期；建议保留本地 ZIP 和校验和。

Run #13 生成了内核映像和诊断信息。ZIP 列表包含 `Image`、`Image.gz`、`kernel.config`、`build.log`、`SHA256SUMS`，但**没有独立的 DTB 或 DTBO 文件**。构建日志出现 `find: ‘arch/arm64/boot/dts/’: No such file or directory`，随后仍继续生成 `Image` 和 `Image.gz`。因此，内核映像步骤成功不代表设备树文件也已构建并打包。

## 本地 boot 镜像证据

本地文件：`~/LG_Wing_Root_Project_Archive/boot_new_base0.img`

使用 `unpack_bootimg` 成功解析镜像。读取到的头部字段：

- Magic：`ANDROID!`
- Header version：2
- Page size：4096
- Kernel size：46,503,952 字节
- Ramdisk size：27,225,758 字节
- Recovery DTBO size：3,056,833 字节
- DTB size：1,212,782 字节
- Kernel load address：`0x8000`
- Ramdisk load address：`0x01000000`
- DTB address：`0x01f00000`
- 显示的 OS patch level：`2127-12`——该值异常且看起来属于未来日期，尚未独立验证。未经原始镜像元数据核实，不应把它作为可信构建值复用。

提取组件的 SHA-256：

| 组件 | SHA-256 | 比对结果 |
|---|---|---|
| 提取的 kernel | `8c6ced13dc396002c76addeb6b6f9d2bf016cf48aa977a7ed769bc8ed38e8ff5` | 与 Run #13 ZIP 中解压得到的未压缩 `Image` 完全一致 |
| 提取的 ramdisk | `b63aeb0373b90a6f04534f74721bdd9d6db25ec257bdc87109c0c87d55351199` | 与本地 `ramdisk_magisk` 完全一致 |
| 提取的 DTB 组件 | `322de263c6463877c82b037c634b743c4ccb104aa439f9e5eabfad36e1032910` | 与本地 `dtb_magisk` 完全一致 |
| 提取的 `recovery_dtbo` | `80256ffbe91bb1b4099966c238f4befd5a65e90001fe7d533036da5245f2d727` | 尚未与独立可信参考文件比较 |
| 原始参考 kernel | `9d0cf9a74991a44ca0e7c912a5ef5b93b20980c3115f4e42084e1a0f1c292f62` | 与新构建内核不同；对于修改后的构建而言，这是预期情况 |

项目归档中记录的完整本地 `boot_new_base0.img` SHA-256 为 `72b29f53d71eeeb3a5e216d49311731363825df9a749e79b6241037da6643940`。此前已确认本地归档与外部存储副本的哈希一致。

DTB 检查工具在 1,212,782 字节的提取组件中识别到 605,249 字节的 FDT 结构。仅凭此结果不能认定组件损坏；组件可能包含额外数据或多个结构。不要仅因该差异就替换 DTB。

## KernelSU 配置证据

打包的 `kernel.config` 中包含 `CONFIG_KSU=y`、`CONFIG_KPROBES=y`、`CONFIG_MODULES=y` 和 `CONFIG_KSU_SYSCALL_TABLE_HOOK=y`。内核映像中也找到了 `kernelsu.allow_shell`、`kernelsu.norc`、`Tksu_avc` 等字符串。这些是辅助证据，但配置值和字符串本身不能证明运行时功能已启用、管理器集成正确或 root 行为成功。

## 更新的 CI 运行与验证变更

- [Run #16](https://github.com/almwld/android_kernel_lge_sm7250/actions/runs/40617303191)，提交 `8ef0c4fffb3dc65a47f4cf9ea582b02aa6933027`：最近一次检查时仍为 `in_progress`；内核/设备树构建步骤仍在运行，验证与产物上传尚未开始。
- [Run #17](https://github.com/almwld/android_kernel_lge_sm7250/actions/runs/40621283683)，提交 `55e037666dea3d96af3360fd0c8ec1e8153459d6`：很快以 `failure` 结束，GitHub jobs 接口未返回任何 job。现有元数据无法确定原因；没有运行日志时，不应将其直接认定为编译失败。
- 提交 `55e037666dea3d96af3360fd0c8ec1e8153459d6` 更新了 workflow：若收集到的 DTB/DTBO 数量为零，则验证失败；同时为所有已收集产物生成校验和，而不再只校验 `Image.gz`。此检查旨在避免把只有内核映像的输出误报为完整设备构建。
- 最近一次检查时，Run #16 尚无产物列表；Run #17 也没有返回 job/产物信息。因此目前两个运行都不能作为已验证的完整设备构建。

## 结论与后续要求

**不能仅凭现有证据刷写 `boot_new_base0.img`。** 内核、ramdisk 和 DTB 的比对只证明组件身份一致，并不证明整机能够正常启动。Recovery DTBO 尚未与可信来源比较，且 Run #13 没有打包设备树输出。

在考虑任何 boot 镜像测试之前：

1. 等待新的构建最终成功，并检查完整产物列表，确认预期设备树输出存在且非空。
2. 将候选 DTB/DTBO 与确切设备型号和固件版本的可信镜像比较；不要仅因为出现了新生成的文件就替换 `recovery_dtbo`。
3. 确认与手机实际厂商内核对应的源码提交，并核实原始镜像的 boot-header 字段。
4. 保留原始 boot 镜像和已验证的恢复路径。此前报告手机 bootloader 已锁定，因此不能假设刷写操作可行或安全。
5. GhostLock 的设备状态应保持 `DETECTED_UNVALIDATED`。不要仅根据内核版本或本次构建审计创建或启用可执行配置。源码观察不能证明已安装厂商内核是否包含回移补丁。

本文仅用于记录证据，不包含利用偏移、运行时内存目标或可执行 GhostLock 配置。
