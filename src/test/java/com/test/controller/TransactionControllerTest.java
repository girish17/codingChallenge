package com.test.controller;

import com.controller.TransactionController;
import com.model.TransactionStatistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Timer;
import java.util.TimerTask;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TransactionControllerTest {
    MockMvc mockMvc;

    @Mock
    private TransactionController transactionController;

    @Autowired
    private TestRestTemplate template;

    private static long count=0;
    private static double sum=0.0;
    private static double max=0.0;
    private static double min=0.0;
    private static double avg=0.0;

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }

    @Test
    public void testNewTransaction() throws Exception {
        HttpEntity<Object> transaction = getHttpEntity("{\"amount\":12.3,\"timestamp\":"+System.currentTimeMillis()+"}");
        ResponseEntity<Void> transactionResponseEntity = template.postForEntity("/transactions",transaction,Void.class);
        Assert.assertEquals(201, transactionResponseEntity.getStatusCode().value());
    }

    @Test
    public void testOldTransaction() throws Exception {
        Long olderTimestamp = System.currentTimeMillis() - 60001;
        HttpEntity<Object> old_transaction = getHttpEntity("{\"amount\":12.3,\"timestamp\":"+olderTimestamp+"}");
        ResponseEntity<Void> old_transactionResponseEntity = template.postForEntity("/transactions",old_transaction,Void.class);
        Assert.assertEquals(204, old_transactionResponseEntity.getStatusCode().value());
    }

    @Test
    public void testStatistics() throws Exception {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                double amount = Math.random();
                HttpEntity<Object> transaction = getHttpEntity("{\"amount\":"+amount+",\"timestamp\":"+System.currentTimeMillis()+"}");
                ResponseEntity<Void> transactionResponseEntity = template.postForEntity("/transactions",transaction,Void.class);

                if(count == 0)
                {
                    max = amount;
                    min = amount;
                }
                sum += amount;
                count++;
                avg = sum/count;
                if(amount > max)
                    max = amount;
                if(amount < min)
                    min = amount;
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 0, 1);
        /*collect results for a minute*/
        Thread.sleep(60000);
        timer.cancel();
        ResponseEntity<TransactionStatistics> transactionResponseEntity = template.getForEntity("/statistics", TransactionStatistics.class);
        Assert.assertEquals(200, transactionResponseEntity.getStatusCode().value());
        Assert.assertEquals(count, transactionResponseEntity.getBody().getCount().longValue());
        Assert.assertEquals(sum, transactionResponseEntity.getBody().getSum().doubleValue(), 0.001);
        Assert.assertEquals(avg, transactionResponseEntity.getBody().getAvg(), 0.001);
        Assert.assertEquals(min, transactionResponseEntity.getBody().getMin(), 0.001);
        Assert.assertEquals(max, transactionResponseEntity.getBody().getMax(), 0.001);

    }

    public static HttpEntity<Object> getHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<Object>(body, headers);
    }
}
