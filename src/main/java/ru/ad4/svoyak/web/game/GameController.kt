package ru.ad4.svoyak.web.game

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.ad4.svoyak.data.entities.Game
import ru.ad4.svoyak.data.services.GameService
import ru.ad4.svoyak.data.services.UserService
import javax.inject.Inject


@RestController
@RequestMapping("/game")
class GameController @Inject constructor(
        val gameService: GameService,
        val userService: UserService
) {

    @RequestMapping("/start")
    fun start(): Game {
        val user = userService.findUserByLogin("daa") ?: (userService.createUser("daa", "123") ?: throw RuntimeException())
        return gameService.createGame(user)
    }
}