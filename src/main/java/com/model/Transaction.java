package com.model;

import java.io.Serializable;


public class Transaction implements Serializable {

    private Double amount;
    private Long timestamp;

    public Transaction(){
      super();
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString(){
        return "Amount: "+getAmount()+", Timestamp: "+getTimestamp();
    }
}
