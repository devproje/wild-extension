package net.projecttl.wild.extension.model

import io.papermc.paper.command.brigadier.Commands

interface BrigadierCommand {
    fun register(node: Commands)
}