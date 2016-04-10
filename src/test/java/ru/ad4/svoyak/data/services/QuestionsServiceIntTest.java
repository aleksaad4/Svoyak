package ru.ad4.svoyak.data.services;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.ad4.svoyak.App;
import ru.ad4.svoyak.data.entities.*;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
@IntegrationTest
@SqlGroup({@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/beforeTestRun.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/afterTestRun.sql")})
public class QuestionsServiceIntTest {

    @Inject
    private QuestionsService questionsService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testQuestionServiceSelect() {
        // проверяем, что есть как минимум 15 тематик
        final List<Topic> topics = questionsService.getTopics(15);
        Assert.assertEquals(15, topics.size());
        // вопросы подгружены
        Assert.assertNotNull(topics.get(0).getQuestionList());
        // ответы подгружены
        Assert.assertNotNull(topics.get(0).getQuestionList().get(0).getAnswerList());

        // проверяем, что если запросить слишком много тематик, то получим exception
        exception.expect(GameException.class);
        questionsService.getTopics(Integer.MAX_VALUE);
    }

    @Test
    public void testQuestionServiceSave() {
        // тест на сохранение сущностей с супер длинным ответом
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append(UUID.randomUUID().toString());
        }
        // ожидаем ошибку
        final Tour tour1 = genTestTour(sb.toString());
        exception.expect(DataIntegrityViolationException.class);
        questionsService.saveTours(Collections.singletonList(tour1));

        // проверяем, что транзакции работают - все наши операции не удались
        final List<Topic> topicByName = questionsService.findTopicByName(tour1.getTopicList().get(0).getName());
        Assert.assertTrue(topicByName.isEmpty());

        // сохраняем
        final Tour tour2 = genTestTour("Ответ №1");
        questionsService.saveTours(Collections.singletonList(tour2));
        Assert.assertNotNull(questionsService.findTopicById(tour2.getTopicList().get(0).getId()));
        Assert.assertFalse(questionsService.findTopicByName(tour2.getTopicList().get(0).getName()).isEmpty());

        // пытаемся сохранить второй раз
        exception.expect(DataIntegrityViolationException.class);
        questionsService.saveTours(Collections.singletonList(genTestTour("Ответ №1")));
    }

    @Nonnull
    private Tour genTestTour(@Nonnull final String answerText) {
        final Answer answer1 = new Answer(answerText, true, false, 0, 0);
        final Answer answer2 = new Answer("Ответ #2", false, true, 0, 0);
        final Question question = new Question("Вопрос #1", 0, 0, Arrays.asList(answer1, answer2));
        final Topic topic = new Topic("Тестовая тематика", 0, 0, Collections.singletonList(question));
        return new Tour("Тестовый турнир", SourceType.DB_CHGK_INFO, 0, Collections.singletonList(topic));
    }
}
