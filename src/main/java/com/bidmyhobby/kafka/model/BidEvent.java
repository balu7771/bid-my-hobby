package com.bidmyhobby.kafka.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidEvent {
    private String itemId;
    private String userId;
    private BigDecimal bidAmount;

    public BidEvent() {
        // Required by Kafka deserializer
    }

    public BidEvent(String itemId, String userId, BigDecimal bidAmount) {
        this.itemId = itemId;
        this.userId = userId;
        this.bidAmount = bidAmount;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    @Override
    public String toString() {
        return "BidEvent{" +
                "itemId='" + itemId + '\'' +
                ", userId='" + userId + '\'' +
                ", bidAmount=" + bidAmount +
                '}';
    }
}
