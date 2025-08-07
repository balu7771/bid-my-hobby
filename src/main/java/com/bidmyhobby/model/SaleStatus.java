package com.bidmyhobby.model;

public enum SaleStatus {
    ACTIVE("Item is available for bidding"),
    BID_APPROVED("Creator approved a bid - awaiting initial payment"),
    SALE_IN_PROGRESS("Initial payment made - sale in progress"),
    SHIPPED("Item shipped - awaiting shipping payment"),
    DELIVERED("Item delivered - awaiting final payment"),
    SOLD("Sale completed"),
    CANCELLED("Sale cancelled"),
    NOT_FOR_SALE("Item not for sale"),
    DELETED("Item deleted");
    
    private final String description;
    
    SaleStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() { return description; }
}