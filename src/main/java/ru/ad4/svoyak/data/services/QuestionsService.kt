package ru.ad4.svoyak.data.services

import org.springframework.stereotype.Service
import ru.ad4.svoyak.data.entities.Tour
import javax.transaction.Transactional

@Service
@Transactional
class QuestionsService {

    fun saveTours(tours: List<Tour>) {
        // todo: сохранение туров, тем, вопросов и ответов в БД
    }
}