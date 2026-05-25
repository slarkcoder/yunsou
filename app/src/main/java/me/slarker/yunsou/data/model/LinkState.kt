package me.slarker.yunsou.data.model

enum class LinkState {
    OK, BAD, LOCKED, UNSUPPORTED, UNCERTAIN, UNCHECKED;

    val displayName: String get() = when (this) {
        OK -> "有效"
        BAD -> "失效"
        LOCKED -> "需密码"
        UNSUPPORTED -> "不支持"
        UNCERTAIN -> "不确定"
        UNCHECKED -> "未检测"
    }

    companion object {
        fun fromApi(state: String): LinkState = when (state) {
            "ok" -> OK
            "bad" -> BAD
            "locked" -> LOCKED
            "unsupported" -> UNSUPPORTED
            "uncertain" -> UNCERTAIN
            else -> UNCHECKED
        }
    }
}
