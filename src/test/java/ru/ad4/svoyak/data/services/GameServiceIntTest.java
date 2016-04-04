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
import ru.ad4.svoyak.data.entities.User;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest
@SqlGroup({@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/beforeTestRun.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/afterTestRun.sql")})
public class GameServiceIntTest {

    @Inject
    private GameService gameService;

    @Inject
    private UserService userService;

    @Test
    public void testUserService() {
        // ищём, но ещё нет
        final User user1 = userService.findUserByLogin("daa");
        Assert.assertNull(user1);

        // создание
        final User user2 = userService.createUser("daa", "123123");
        Assert.assertNotNull(user2);
        Assert.assertTrue(user2.getId() != 0);

        // ищём то, что создали
        final User user3 = userService.findUserByLogin("daa");
        Assert.assertEquals(user2, user3);

        // проверяем, что нельзя создать пользователей с одинаковым логином
        final User user4 = userService.createUser("daa", "12");
        Assert.assertNull(user4);
    }
}
