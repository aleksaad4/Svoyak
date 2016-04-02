package ru.ad4.svoyak.data.repositories

import org.springframework.data.jpa.repository.JpaRepository
import ru.ad4.svoyak.data.entities.Answer
import ru.ad4.svoyak.data.entities.Question
import ru.ad4.svoyak.data.entities.Topic
import ru.ad4.svoyak.data.entities.Tour

interface TourRepo : JpaRepository<Tour, Long> {
}

interface TopicRepo : JpaRepository<Topic, Long> {
}

interface QuestionRepo : JpaRepository<Question, Long> {
}

interface AnswerRepo : JpaRepository<Answer, Long> {
}