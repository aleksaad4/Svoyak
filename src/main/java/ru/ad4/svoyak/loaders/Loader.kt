package ru.ad4.svoyak.loaders

import ru.ad4.svoyak.data.entities.SourceType
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Интерфейс для загрузки вопросов с удалённого ресурса
 */
interface Loader {
    /**
     * Тип источника
     */
    fun source(): SourceType

    /**
     * Загрузка вопросов из источника и сохранение их в БД
     */
    fun load()
}

/**
 * Функция очистки строк (вопросов, ответов и т. д.)
 */
fun String.beautiful(): String {
    return this.trim()
            .replace("\n", " ")
            .replace(Regex(" +"), " ")
}

/**
 * Функция получения stack trac-а экспешена как строки
 */
fun Exception.getStackTrace(): String {
    val sw = StringWriter();
    val pw = PrintWriter(sw);
    this.printStackTrace(pw);
    return sw.toString();
}