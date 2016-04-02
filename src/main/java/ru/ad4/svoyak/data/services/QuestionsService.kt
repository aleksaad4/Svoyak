package ru.ad4.svoyak.data.services

import com.jcabi.log.Logger
import org.springframework.stereotype.Service
import ru.ad4.svoyak.data.entities.Tour
import ru.ad4.svoyak.data.repositories.AnswerRepo
import ru.ad4.svoyak.data.repositories.QuestionRepo
import ru.ad4.svoyak.data.repositories.TopicRepo
import ru.ad4.svoyak.data.repositories.TourRepo
import javax.inject.Inject

@Service
class QuestionsService @Inject constructor(val qRepo: QuestionRepo,
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
}