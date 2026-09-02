package io.github.chos1n11111.dongqiudipure.core.model

data class NewsCategory(
    val id: String,
    val label: String,
)

enum class CommentOrder {
    Recommended,
    Newest,
}
