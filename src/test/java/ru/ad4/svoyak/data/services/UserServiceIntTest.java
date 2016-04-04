package ru.ad4.svoyak.data.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.ad4.svoyak.App;
import ru.ad4.svoyak.data.entities.Game;
import ru.ad4.svoyak.data.entities.PlayerType;
import ru.ad4.svoyak.data.entities.User;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest
@SqlGroup({@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/beforeTestRun.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/afterTestRun.sql")})
public class UserServiceIntTest {

    @Inject
    private UserService userService;

    @Inject
    private GameService gameService;

    @Test
    public void testUserService() {
        final User user = userService.createUser("daa", "123123");
        Assert.assertNotNull(user);

        // создаём игру и проверяем что в ней все корректно заполнено
        final Game game = gameService.createGame(user);
        Assert.assertFalse(game.getFinished());
        Assert.assertEquals(game.getLevelIndex(), 1);
        Assert.assertEquals(user.getId(), game.getUserId());
        // что есть уровни
        Assert.assertFalse(game.getLevels().isEmpty());
        // что есть игроки
        Assert.assertFalse(game.getPlayers().isEmpty());
        // что есть темы, в них вопросы, а вних ответы
        Assert.assertEquals(game.getId(), game.getLevels().get(0).getGameId());
        Assert.assertEquals(1, game.getLevels().get(0).getIndex());
        Assert.assertFalse(game.getLevels().get(0).getFinished());
        // level topics
        Assert.assertFalse(game.getLevels().get(0).getLevelTopics().isEmpty());
        Assert.assertEquals(game.getLevels().get(0).getId(),
                game.getLevels().get(0).getLevelTopics().get(0).getLevelId());
        // game question
        Assert.assertFalse(game.getLevels().get(0).getLevelTopics().get(0).getGameQuestions().isEmpty());
        Assert.assertEquals(game.getLevels().get(0).getLevelTopics().get(0).getId(),
                game.getLevels().get(0).getLevelTopics().get(0).getGameQuestions().get(0).getLevelTopicId());
        Assert.assertFalse(game.getLevels().get(0).getLevelTopics().get(0).getGameQuestions().get(0).getQuestion().isEmpty());
        Assert.assertFalse(game.getLevels().get(0).getLevelTopics().get(0).getGameQuestions().get(0)
                .getQuestion().get(0).getAnswerList().isEmpty());
        // что среди игроков есть user и это текущий user
        Assert.assertEquals(user.getId(), game.getPlayers().get(0).getUserId());
        Assert.assertEquals(PlayerType.USER, game.getPlayers().get(0).getType());
        Assert.assertEquals(game.getId(), game.getPlayers().get(0).getGameId());
        Assert.assertEquals(user.getLogin(), game.getPlayers().get(0).getName());
        Assert.assertEquals(game.getLevels().get(0).getTurnPlayerId(), game.getPlayers().get(0).getId());
    }
}
