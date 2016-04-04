package ru.ad4.svoyak.loaders.chgk

import com.jcabi.log.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.ad4.svoyak.data.entities.*
import ru.ad4.svoyak.data.services.QuestionsService
import ru.ad4.svoyak.loaders.Loader
import ru.ad4.svoyak.loaders.beautiful
import ru.ad4.svoyak.loaders.getStackTrace
import ru.ad4.svoyak.loaders.xmlFromUrl
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Загрузчик вопросов с сайта ЧГК
 */
@Service
class ChgkLoader @Inject constructor(val qService: QuestionsService,

                                     /**
                                      * Ссылка для получения списка турниров (с вопросами)
                                      */
                                     @Value("\${loaders.chgk.tours-url}")
                                     val toursUrl: String,

                                     /**
                                      * Шаблон ссылки для загрузки вопрос из турнира
                                      * tourName в url-е нужно заменить на реальное навазние турнира
                                      */
                                     @Value("\${loaders.chgk.topic-template-url:}")
                                     val topicTemplateUrl: String) : Loader {

    override fun source(): SourceType {
        return SourceType.DB_CHGK_INFO
    }

    override fun load(): Boolean {
        Logger.info(this, "Load topics and questions from CHGK..")

        try {
            // загружаем все турниры
            val tourNames = loadTourNames()
            Logger.debug(this, "Get [$tourNames.size] tour names")

            // для каждого турнира загружаем темы с вопросами
            val tourList = ArrayList<Tour>()
            tourNames.forEach {
                try {
                    tourList.add(loadTour(it))
                } catch(e: Exception) {
                    Logger.warn(this, "FAIL, can't load question for tour [$it], cause exception [$e] with stack trace [${e.getStackTrace()}], skip it")
                }
            }
            Logger.debug(this, "Get topics for [$tourNames.size] tours")

            // сохранение загруженных давнных в БД
            qService.saveTours(tourList)

            Logger.info(this, "OK, success load and save topics and questions from CHGK")
            return true
        } catch(e: Exception) {
            Logger.error(this, "FAIL, can't load topics and questions from CHGK, catch exception [$e] with stack trace [${e.getStackTrace()}]")
            return false
        }
    }

    /**
     * Загружаем вопросы по турниру с данным названием (textId)
     */
    private fun loadTour(tourTextId: String): Tour {
        Logger.debug(this, "Load questions for tour [$tourTextId]")

        // получаем xml из url-а (подставляем textId вместо 'tourName')
        val doc = xmlFromUrl(topicTemplateUrl.replace("tourName", tourTextId))

        // элементы с вопросами и ответами по каждой теме в турнире
        val questionElements = doc.getElementsByTagName("Question")
        val answerElements = doc.getElementsByTagName("Answer")

        // проверяем, что ответов и вопросов равное количество
        if (questionElements.length != answerElements.length) {
            throw RuntimeException("Not same count question and answer elements in tour [$tourTextId]")
        }

        // регулярные выражения для ответов и вопросов
        val qPattern = Pattern.compile("(.*)1\\.(.*)2\\.(.*)3\\.(.*)4\\.(.*)5\\.(.*)", Pattern.DOTALL)
        val aPattern = Pattern.compile("(.*)1\\.(.*)2\\.(.*)3\\.(.*)4\\.(.*)5\\.(.*)", Pattern.DOTALL)

        val topicList = ArrayList<Topic>()

        for (i in 0..(questionElements.length - 1)) {
            // вопросы и ответы
            val questions = questionElements.item(i).textContent
            val answers = answerElements.item(i).textContent

            // матчим регулярки
            val qM = qPattern.matcher(questions)
            val aM = aPattern.matcher(answers)

            if (qM.matches() && aM.matches()) {
                // название темы
                val topicName = qM.group(1).beautiful()

                // список вопросов в теме
                val qList = ArrayList<Question>()
                for (j in 2..5) {
                    // вопрос
                    val q = qM.group(j).beautiful()
                    // ответ
                    val a = aM.group(j).beautiful()

                    // добавляем вопрос с ответом в список
                    qList.add(Question(q, 0, 0, ArrayList(Arrays.asList(Answer(a, true)))))
                }

                // добавляем тему с вопросами
                topicList.add(Topic(topicName, 0, 0, qList))
            } else {
                // регулярки не сматчились, пропускаем эту тематику
                Logger.debug(this, "FAIL, can't add topic on index [$i] in tour [$tourTextId], answer[$answers] or question [$questions] not matches, skip this topic")
            }
        }

        // вовзращаем tour
        Logger.debug(this, "OK, load [$topicList.size] for tour [$tourTextId]")
        return Tour(tourTextId, source(), 0, topicList)
    }

    /**
     * Получение списка названий турниров, из которых можно получить вопросы
     */
    private fun loadTourNames(): List<String> {
        Logger.debug(this, "Load tours list..")

        val topicNames = ArrayList<String>()

        // получаем все элементы с названиями
        val doc = xmlFromUrl(toursUrl)
        val topicIdElements = doc.getElementsByTagName("TextId")

        // для каждого (начиная со второго, первый - это сам этот файл) добавляем в список
        for (i in 1..(topicIdElements.length - 1)) {
            topicNames.add(topicIdElements.item(i).textContent)
        }

        Logger.debug(this, "OK, load [$topicNames.size] tours")
        return topicNames
    }
}