package ru.ad4.svoyak.loaders.chgk;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.ad4.svoyak.App;

import javax.inject.Inject;
import javax.sql.DataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest
@SqlGroup({@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/afterTestRun.sql")})
public class ChgkLoaderIntTest {

    @Inject
    private ChgkLoader chgkLoader;

    @Inject
    private DataSource dataSource;

    @Test
    public void testChgkLoader() {
        boolean loadAndSave = chgkLoader.load();
        Assert.assertEquals(loadAndSave, true);
    }

    private void dumpToFile() {
        new JdbcTemplate(dataSource).execute("SCRIPT TO 'I:\\Projects\\Svoyak\\src\\test\\resources\\sql\\beforeTestRun.sql'");
    }
}
