package ru.ad4.svoyak.data.repositories

import org.springframework.data.jpa.repository.JpaRepository
import ru.ad4.svoyak.data.entities.*

interface GameRepo : JpaRepository<Game, Long> {
}

interface LevelRepo : JpaRepository<Level, Long> {
}

interface PlayerRepo : JpaRepository<Player, Long> {
}

interface GameTopicRepo : JpaRepository<LevelTopic, Long> {
}

interface GameQuestionRepo : JpaRepository<GameQuestion, Long> {
}

