package ru.ad4.svoyak.seсurity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.ad4.svoyak.App;
import ru.ad4.svoyak.data.entities.User;
import ru.ad4.svoyak.data.services.UserService;
import ru.ad4.svoyak.security.AuthoritiesConstants;
import ru.ad4.svoyak.security.UserDetailsService;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest
@SqlGroup({@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/afterTestRun.sql")})
public class UserDetailsServiceIntTest {

    @Inject
    private UserDetailsService userDetailsService;

    @Inject
    private UserService userService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testUserDetailsService() {
        // ищём то, чего нет
        exception.expect(UsernameNotFoundException.class);
        userDetailsService.loadUserByUsername("daa");

        // создаем пользователя
        final User user = userService.createUser("daa", "123123");
        Assert.assertNotNull(user);

        // ищём
        final UserDetails ud = userDetailsService.loadUserByUsername("daa");

        // проверяем
        Assert.assertNotNull(ud);
        Assert.assertEquals(ud.getAuthorities().size(), 1);
        //noinspection OptionalGetWithoutIsPresent
        Assert.assertEquals(ud.getAuthorities().stream().findFirst().get().getAuthority(), AuthoritiesConstants.INSTANCE.getUSER());
        Assert.assertEquals(ud.getUsername(), "daa");
        Assert.assertEquals(ud.getPassword(), "123123");
    }
}
