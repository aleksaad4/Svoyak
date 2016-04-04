package ru.ad4.svoyak.data.services

import com.jcabi.log.Logger
import org.springframework.stereotype.Service
import ru.ad4.svoyak.data.entities.*
import ru.ad4.svoyak.data.repositories.*
import java.util.*
import javax.inject.Inject
import javax.transaction.Transactional


@Service
@Transactional
class GameService @Inject constructor(val gameRepo: GameRepo,
                                      val playerRepo: PlayerRepo,
                                      val levelRepo: LevelRepo,
                                      val questionsService: QuestionsService,
                                      val topicRepo: GameTopicRepo,
                                      val questionRepo: GameQuestionRepo) {
    /**
     * Количество уровней в игре
     */
    val levelCount: Int = 3

    /**
     * Количество уровней в игре
     */
    val playerCount: Int = 3

    /**
     * Количество тем в уровне
     */
    val levelTopicCount: Int = 5

    /**
     * Начальная цена первого вопроса первого раунда
     */
    val startScore: Int = 100

    /**
     * Список имен для AI игроков
     */
    val playerNames: List<String> = listOf("Люк", "Лея", "Оби-Ваy", "Дарт Вейдер", "Р2-Д2", "С-3PO")

    /**
     * Создание игры для пользователя user
     */
    fun createGame(user: User): Game {
        Logger.info(this, "Create game for user [$user]")

        // создаём игру
        val game = gameRepo.save(Game(user.id))

        // создаём игроков
        val players = ArrayList<Player>()
        for (i in 1..playerCount) {
            when (i) {
                1 -> players.add(playerRepo.save(Player(game.id, user.login, PlayerType.USER, user.id)))
                else -> players.add(playerRepo.save(Player(game.id, playerNames.randomGet(players.map { it.name }))))
            }
        }
        game.players = players
        Logger.debug(this, "Players [${players.map { it.name }}] was created")


        // выбираем тематики для игры
        val gameTopics = questionsService.getTopics(levelCount * levelTopicCount)
        Logger.debug(this, "Was selected [${gameTopics.size}] topics")

        // создаём раунды
        val levels = ArrayList<Level>()
        for (i in 0..(levelCount - 1)) {
            val level = levelRepo.save(Level(game.id, players[0].id, i + 1))
            Logger.debug(this, "Level [$level] was created")

            val topics = ArrayList<LevelTopic>()
            for (j in 0..(levelTopicCount - 1)) {
                // создаём тематику
                val gTopic = gameTopics[i * levelTopicCount + j]
                val levelTopic = topicRepo.save(LevelTopic(level.id, gTopic.id))
                topics.add(levelTopic)

                // создаём вопросы в тематиках
                val gameQuestion = gTopic.questionList.map {
                    questionRepo.save(GameQuestion(it.id, qScore(level.index, j + 1), levelTopic.id, false, 0, gTopic.questionList))
                }
                levelTopic.gameQuestions = gameQuestion
            }
            level.levelTopics = topics
            levels.add(level)
        }
        game.levels = levels

        Logger.info(this, "Game [$game] was created")
        return game
    }

    /**
     * Получение количество очков для вопроса под номерм qIndex на levelIndex раундеы
     */
    private fun qScore(levelIndex: Int, qIndex: Int): Int {
        return startScore * levelIndex * qIndex
    }

}

/**
 * Функция получение случайного элемента из списка
 * С возможностью исключить из списка forExclude элементы
 */
fun <T> List<T>.randomGet(forExclude: List<T> = ArrayList<T>()): T {
    val afterExclude = this.filter { !forExclude.contains(it) }
    return afterExclude[Random().nextInt(afterExclude.size)]
}

class GameException(msg: String) : Exception(msg) {

}