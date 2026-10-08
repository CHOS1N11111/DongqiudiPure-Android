package io.github.chos1n11111.dongqiudipure.core.data

/** One instance per PagingSource; only commit IDs after the entire page is validated. */
internal class PagingDeduplicator {
    private val seen = mutableSetOf<String>()

    fun <T> filter(items: List<T>, refresh: Boolean, id: (T) -> String): List<T> {
        if (refresh) seen.clear()
        return items.filter { seen.add(id(it)) }
    }
}
