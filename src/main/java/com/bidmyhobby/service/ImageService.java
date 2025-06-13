package com.bidmyhobby.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageService {

    public byte[] addWatermark(MultipartFile file, String watermarkText) throws IOException {
        // Read the image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        // Create a graphics context on the buffered image
        Graphics2D g2d = originalImage.createGraphics();
        
        // Set font properties for watermark
        int fontSize = Math.max(originalImage.getWidth() / 20, 14); // Scale font size based on image width
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);
        
        // Set semi-transparent white color for better visibility on any background
        g2d.setColor(new Color(255, 255, 255, 180));
        
        // Calculate position (top-right corner with padding)
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int padding = 10;
        int x = originalImage.getWidth() - fontMetrics.stringWidth(watermarkText) - padding;
        int y = fontMetrics.getHeight() + padding;
        
        // Draw shadow for better readability
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.drawString(watermarkText, x + 2, y + 2);
        
        // Draw the actual watermark text
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.drawString(watermarkText, x, y);
        
        // Dispose the graphics context
        g2d.dispose();
        
        // Convert the buffered image back to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, getImageFormat(file.getOriginalFilename()), baos);
        
        return baos.toByteArray();
    }
    
    public String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email; // Too short to mask
        
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        // Show first 2 chars and last char of username
        String maskedUsername = username.substring(0, 2) + 
                               "*".repeat(Math.max(0, username.length() - 3)) + 
                               username.substring(username.length() - 1);
        
        return maskedUsername + domain;
    }
    
    private String getImageFormat(String filename) {
        if (filename == null) {
            return "jpeg";
        }
        
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        
        // Default to JPEG if format is not recognized
        if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("jpeg") && !extension.equals("gif")) {
            return "jpeg";
        }
        
        return extension.equals("jpg") ? "jpeg" : extension;
    }
}