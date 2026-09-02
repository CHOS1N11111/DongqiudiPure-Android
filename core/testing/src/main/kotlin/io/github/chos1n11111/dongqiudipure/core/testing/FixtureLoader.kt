package io.github.chos1n11111.dongqiudipure.core.testing

object FixtureLoader {

    fun read(path: String, classLoader: ClassLoader): String =
        requireNotNull(classLoader.getResource(path)) { "Missing fixture: $path" }
            .readText(Charsets.UTF_8)
}
