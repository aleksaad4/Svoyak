package ru.ad4.svoyak.data.repositories

import org.springframework.data.jpa.repository.JpaRepository
import ru.ad4.svoyak.data.entities.Answer
import ru.ad4.svoyak.data.entities.Question
import ru.ad4.svoyak.data.entities.Topic
import ru.ad4.svoyak.data.entities.Tour

interface TourRepo : JpaRepository<Tour, Long> {
}

interface TopicRepo : JpaRepository<Topic, Long> {
    fun findByName(topicName: String): List<Topic>
}

interface QuestionRepo : JpaRepository<Question, Long> {
    fun findByTopicId(topicId: Long): List<Question>

}

interface AnswerRepo : JpaRepository<Answer, Long> {
    fun findByQuestionId(topicId: Long): List<Answer>
}