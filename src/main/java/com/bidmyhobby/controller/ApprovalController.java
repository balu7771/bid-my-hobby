package com.bidmyhobby.controller;

// TODO: Add Netflix Conductor dependency back when needed
// import com.netflix.conductor.client.http.WorkflowClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/approve")
public class ApprovalController {

    // TODO: Uncomment when Conductor is added
    // @Autowired
    // private WorkflowClient workflowClient;

    @GetMapping
    public RedirectView handleApproval(
            @RequestParam String token,
            @RequestParam String action) {
        
        // TODO: Implement with Conductor later
        if ("APPROVED".equals(action)) {
            return new RedirectView("/approval-success?message=Bid approved successfully!");
        } else {
            return new RedirectView("/approval-success?message=Bid rejected successfully!");
        }
    }
}