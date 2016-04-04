package ru.ad4.svoyak.data.services

import com.jcabi.log.Logger
import org.springframework.stereotype.Service
import ru.ad4.svoyak.data.entities.Topic
import ru.ad4.svoyak.data.entities.Tour
import ru.ad4.svoyak.data.repositories.AnswerRepo
import ru.ad4.svoyak.data.repositories.QuestionRepo
import ru.ad4.svoyak.data.repositories.TopicRepo
import ru.ad4.svoyak.data.repositories.TourRepo
import java.util.*
import javax.inject.Inject
import javax.transaction.Transactional

@Service
@Transactional
open class QuestionsService @Inject constructor(val qRepo: QuestionRepo,
                                                val aRepo: AnswerRepo,
                                                val tourRepo: TourRepo,
                                                val topicRepo: TopicRepo) {

    /**
     * Сохранение турниров (тем, вопросов и ответов)
     */
    fun saveTours(tours: List<Tour>) {
        Logger.info(this, "Save [${tours.size}] tours")

        tours.forEach {
            // сохраняем турнир
            val tour = tourRepo.save(it)
            it.topicList.forEach {
                // сохраняем тему
                it.tourId = tour.id
                val topic = topicRepo.save(it)
                it.questionList.forEach {
                    // сохраняем вопрос
                    it.topicId = topic.id
                    val q = qRepo.save(it)
                    it.answerList.forEach {
                        // сохраняем ответ
                        it.questionId = q.id
                        aRepo.save(it)
                    }
                }
            }
        }

        Logger.info(this, "OK, [${tours.size}] was saved")
    }

    /**
     * Получение указанного количества тем с подгруженными вопросами и ответами
     */
    fun getTopics(count: Int): List<Topic> {
        Logger.info(this, "Get [$count] random topics")

        // достаём все тематики
        val allTopics = topicRepo.findAll()
        Logger.debug(this, "Find [${allTopics.size}] topics")

        // перемешиваем тематики
        Collections.shuffle(allTopics)

        // если тематик недостаточно - кидаем exception
        if (allTopics.size < count) {
            throw GameException("Not enough topics [${allTopics.size}] (need [$count]) for create game")
        }

        // выбираем тематики
        val selectedTopics = allTopics.subList(0, count)

        // загружаем вопросы и ответы по выбранным тематикам
        selectedTopics.forEach {
            it.questionList = qRepo.findByTopicId(it.id)
            it.questionList.forEach {
                it.answerList = aRepo.findByQuestionId(it.id)
            }
        }

        Logger.info(this, "Was selected [$selectedTopics] topics")
        return selectedTopics
    }

    fun findTopicById(id: Long): Topic? {
        Logger.debug(this, "Find topic by id [$id]")

        // ещём тему по id
        val topic = topicRepo.findOne(id)

        Logger.debug(this, "OK, get [$topic] topic")
        return topic
    }

    fun findTopicByName(name: String): List<Topic> {
        Logger.debug(this, "Find topics by name [$name]")

        // ещём тему по id
        val topics = topicRepo.findByName(name)

        Logger.debug(this, "OK, get [$topics] topics")
        return topics
    }
}