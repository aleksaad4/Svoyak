package ru.ad4.svoyak.data.services;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.ad4.svoyak.App;
import ru.ad4.svoyak.data.entities.AuthToken;
import ru.ad4.svoyak.data.entities.User;

import javax.inject.Inject;
import java.time.LocalDate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest
@SqlGroup({@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/afterTestRun.sql")})
public class UserServiceIntTest {

    @Inject
    private UserService userService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testUser() {
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

    @Test
    public void testAuthToken() {
        // создаём юзера
        final User user = userService.createUser("daa", "123123");
        Assert.assertNotNull(user);

        // создаём токен
        final AuthToken token1 = new AuthToken("token", LocalDate.now(), "127.0.0.1", "android", user, "series");
        final AuthToken token2 = userService.saveAuthToken(token1);
        Assert.assertNotNull(token2);

        // ищем то, что создали
        final AuthToken token3 = userService.findAuthToken("series");
        Assert.assertNotNull(token3);
        Assert.assertNotNull(token3.getUser());

        // пытаемся сохранить второй раз
        exception.expect(DuplicateKeyException.class);
        final AuthToken token4 = new AuthToken("token", LocalDate.now(), "127.0.0.1", "android", user, "series");
        final AuthToken token5 = userService.saveAuthToken(token4);
        Assert.assertNull(token5);

        // удаляем token1
        userService.removeAuthToken(token1);
        // не находим то, что удалили
        final AuthToken token6 = userService.findAuthToken("token");
        Assert.assertNull(token6);
    }
}
