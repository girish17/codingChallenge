package com.controller;

import com.model.Transaction;
import com.model.TransactionStatistics;
import org.springframework.data.annotation.Transient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;


@RestController
public class TransactionController {

    /* O(1) space complexity*/
    @Transient
    TransactionStatistics transactionStatistics = new TransactionStatistics(0.0,0.0,0.0,0.0,0L);

    @PostMapping(path = "/transactions")
    public ResponseEntity<Void> doTransaction(@RequestBody Transaction transaction){
        Logger logger = Logger.getLogger("TransactionLogger");
        logger.log(Level.INFO, transaction.toString());
        if(System.currentTimeMillis()-transaction.getTimestamp()>60000)
        {
            return ResponseEntity.status(HttpStatus.valueOf(204)).body(null);
        }
        else
        {
            if(transactionStatistics.getCount() == 0)
            {
                /*initialize*/
                transactionStatistics.setMax(transaction.getAmount());
                transactionStatistics.setMin(transaction.getAmount());
            }

            transactionStatistics.setCount(transactionStatistics.getCount().longValue()+1);

            transactionStatistics.setSum(transactionStatistics.getSum()+transaction.getAmount());

            transactionStatistics.setAvg(transactionStatistics.getSum()/transactionStatistics.getCount());

            if(transaction.getAmount() > transactionStatistics.getMax())
            {
                transactionStatistics.setMax(transaction.getAmount());
            }
            if(transaction.getAmount() < transactionStatistics.getMin())
            {
                transactionStatistics.setMin(transaction.getAmount());
            }
            return ResponseEntity.status(HttpStatus.valueOf(201)).body(null);
        }
    }

    @GetMapping(path = "/statistics")
    public ResponseEntity<TransactionStatistics> getStatistics(){
        Logger logger = Logger.getLogger("TransactionStatsLogger");
        logger.log(Level.INFO, transactionStatistics.toString());
        /* O(1) time complexity */
        return ResponseEntity.ok(transactionStatistics);
    }
}
