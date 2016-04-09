package ru.ad4.svoyak.utils

/**
 * Применяет функцию f к объекту, если он не null, иначе кидает экспешн e
 */
inline fun <T : Any?, R, E : Exception> T?.mapOrThrow(f: (T) -> R, e: E): R {
    val o = this ?: throw e;
    return f(o);
}

/**
 * Применяет функцию f к объекту, если он не null, иначе возвращает null
 */
inline fun <T : Any?, R> T?.mapOrNull(f: (T) -> R): R? {
    val o = this ?: return null;
    return f(o);
}


/**
 * Объект в список
 */
fun <T : Any> T.toList(): List<T> {
    return listOf(this)
}
