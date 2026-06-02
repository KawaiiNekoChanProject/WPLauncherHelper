package net.nekocurit.utils

suspend fun <Entity> easyMultiPage(page: Int = 0, length: Int = 50, baseLength: Int, block: suspend (realPage: Int) -> Collection<Entity>) = mutableListOf<Entity>()
    .apply {
        val start = page * length
        val end = start + length

        val queryIndex = start - start % baseLength
        val queryPageStart = queryIndex / baseLength

        run {
            repeat(((end - start + baseLength - 1) / baseLength) + if (start % baseLength == 0) 0 else 1) { i ->
                block(queryPageStart + i)
                    .also { if (it.isEmpty()) return@run }
                    .forEachIndexed { j, simple ->
                        if (i == 0 && queryIndex + j < start) return@forEachIndexed
                        if (size == length) return@apply

                        add(simple)
                    }
            }
        }
    }