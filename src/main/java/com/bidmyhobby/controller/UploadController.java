// This file is deprecated. All upload functionality has been consolidated into BidController.uploadItem()
// Please use /api/bid/uploadItem endpoint instead
package com.bidmyhobby.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UploadController {
    // Functionality moved to BidController.uploadItem()
}