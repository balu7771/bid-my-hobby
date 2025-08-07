package com.bidmyhobby.conductor.tasks;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentTaskWorker implements Worker {

    @Autowired
    private PaymentService paymentService;

    @Override
    public String getTaskDefName() {
        return "hold_payment";
    }

    @Override
    public TaskResult execute(Task task) {
        TaskResult result = new TaskResult(task);
        
        try {
            String paymentMethod = (String) task.getInputData().get("paymentMethod");
            Double amount = (Double) task.getInputData().get("amount");
            String bidId = (String) task.getInputData().get("bidId");
            
            // Hold payment using Razorpay
            String paymentId = paymentService.holdPayment(paymentMethod, amount, bidId);
            
            result.setStatus(TaskResult.Status.COMPLETED);
            result.getOutputData().put("paymentId", paymentId);
            result.getOutputData().put("status", "HELD");
            
        } catch (Exception e) {
            result.setStatus(TaskResult.Status.FAILED);
            result.setReasonForIncompletion(e.getMessage());
        }
        
        return result;
    }
}