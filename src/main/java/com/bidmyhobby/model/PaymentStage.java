package com.bidmyhobby.model;

public enum PaymentStage {
    INITIAL_10_PERCENT("INITIAL", 0.10, "Initial payment when bid is approved"),
    SHIPPING_50_PERCENT("SHIPPING", 0.50, "Payment when item is shipped"),
    FINAL_40_PERCENT("FINAL", 0.40, "Final payment when item is received");
    
    private final String code;
    private final double percentage;
    private final String description;
    
    PaymentStage(String code, double percentage, String description) {
        this.code = code;
        this.percentage = percentage;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public double getPercentage() { return percentage; }
    public String getDescription() { return description; }
}