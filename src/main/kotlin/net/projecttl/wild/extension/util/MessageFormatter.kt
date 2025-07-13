package net.projecttl.wild.extension.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

fun String.toMini(): Component {
    return MiniMessage.miniMessage().deserialize(this)
}

fun Component.asString(): String {
    return MiniMessage.miniMessage().serialize(this)
}
