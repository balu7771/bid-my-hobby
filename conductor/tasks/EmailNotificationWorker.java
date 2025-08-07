package com.bidmyhobby.conductor.tasks;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationWorker implements Worker {

    @Autowired
    private EmailService emailService;

    @Override
    public String getTaskDefName() {
        return "notify_creator";
    }

    @Override
    public TaskResult execute(Task task) {
        TaskResult result = new TaskResult(task);
        
        try {
            String creatorEmail = (String) task.getInputData().get("creatorEmail");
            String bidDetails = (String) task.getInputData().get("bidDetails");
            String approvalToken = (String) task.getInputData().get("approvalToken");
            
            // Send approval email with approve/reject links
            String approveUrl = "https://bidmyhobby.com/approve?token=" + approvalToken + "&action=APPROVED";
            String rejectUrl = "https://bidmyhobby.com/approve?token=" + approvalToken + "&action=REJECTED";
            
            String emailContent = buildApprovalEmail(bidDetails, approveUrl, rejectUrl);
            
            emailService.sendEmail(creatorEmail, "New Bid Approval Required", emailContent);
            
            result.setStatus(TaskResult.Status.COMPLETED);
            result.getOutputData().put("emailSent", true);
            
        } catch (Exception e) {
            result.setStatus(TaskResult.Status.FAILED);
            result.setReasonForIncompletion(e.getMessage());
        }
        
        return result;
    }
    
    private String buildApprovalEmail(String bidDetails, String approveUrl, String rejectUrl) {
        return String.format("""
            <h2>New Bid Received!</h2>
            <p>%s</p>
            <div style="margin: 20px 0;">
                <a href="%s" style="background: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                    APPROVE BID
                </a>
                <a href="%s" style="background: #dc3545; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin-left: 10px;">
                    REJECT BID
                </a>
            </div>
            <p><small>This approval expires in 48 hours.</small></p>
            """, bidDetails, approveUrl, rejectUrl);
    }
}