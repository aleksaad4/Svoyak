package ru.ad4.svoyak.data.entities

import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id


@Entity
data class Game(
        val userId: Long = 0,
        var finished: Boolean = false,
        var levelIndex: Int = 1,

        @Id @GeneratedValue
        var id: Long = 0,

        @Transient
        var players: List<Player> = ArrayList<Player>(),

        @Transient
        var levels: List<Level> = ArrayList<Level>()
)

@Entity
data class Level(
        var gameId: Long,
        var turnPlayerId: Long = 0,
        var index: Int = 1,
        var finished: Boolean = false,

        @Id @GeneratedValue
        var id: Long = 0,

        @Transient
        var levelTopics: List<LevelTopic> = ArrayList<LevelTopic>()
)


@Entity
data class Player(
        var gameId: Long,
        var name: String = "",
        var type: PlayerType = PlayerType.AI,
        var userId: Long = 0,
        var score: Int = 0,

        @Id @GeneratedValue
        var id: Long = 0
)

enum class PlayerType {
    USER, AI
}


@Entity
data class LevelTopic(
        var levelId: Long,
        var topicId: Long = 0,

        @Id @GeneratedValue
        var id: Long = 0,

        @Transient
        var gameQuestions: List<GameQuestion> = ArrayList<GameQuestion>()
)

@Entity
data class GameQuestion(
        var qId: Long = 0,
        var score: Int = 0,
        var levelTopicId: Long = 0,
        var answered: Boolean = false,

        @Id @GeneratedValue
        var id: Long = 0,

        @Transient
        var question: List<Question> = ArrayList<Question>()
)