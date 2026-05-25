package me.slarker.yunsou.data.model

enum class CloudType(val apiName: String, val displayName: String, val packageNames: List<String> = emptyList()) {
    BAIDU("baidu", "百度网盘", listOf("com.baidu.netdisk")),
    ALIYUN("aliyun", "阿里云盘", listOf("com.alicloud.databox")),
    QUARK("quark", "夸克网盘", listOf("com.quark.clouddrive", "com.quark.browser")),
    TIANYI("tianyi", "天翼云盘", listOf("com.cn21.ecloud")),
    UC("uc", "UC网盘", listOf("com.UCMobile")),
    XUNLEI("xunlei", "迅雷", listOf("com.xunlei.downloadprovider")),
    PIKPAK("pikpak", "PikPak", emptyList()),
    GUANGYA("guangya", "光照", emptyList()),
    MOBILE("mobile", "移动", listOf("com.chinamobile.mcloud")),
    CLOUD_115("115", "115", listOf("com.yyw.wangpan")),
    CLOUD_123("123", "123", listOf("com.mfcloudcalculate.networkdisk")),
    MAGNET("magnet", "磁力链接", emptyList()),
    ED2K("ed2k", "电驴", emptyList());

    companion object {
        fun fromApiName(name: String): CloudType? =
            entries.find { it.apiName == name }

        fun defaultSet(): Set<CloudType> =
            setOf(BAIDU, ALIYUN, QUARK, TIANYI, UC)
    }
}
