package com.bidmyhobby.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemUploadEvent {
    private String itemId;
    private String creatorId;
    private String creatorName;
    private String itemTitle;
    private String itemDescription;
    private String imageUrl;
    private Double startingBid;
    private LocalDateTime uploadTime;
    private String category;
}