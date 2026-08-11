package com.fleetops.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "dashboard"; // redirect to dashboard or login
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/vehicles")
    public String vehicles() {
        return "vehicles";
    }

    @GetMapping("/drivers")
    public String drivers() {
        return "drivers";
    }

    @GetMapping("/fuel")
    public String fuel() {
        return "fuel";
    }

    @GetMapping("/maintenance")
    public String maintenance() {
        return "maintenance";
    }

    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }

    @GetMapping("/audit")
    public String auditLogs() {
        return "audit";
    }


    @GetMapping("/deleted-records")
    public String deletedRecords() {
        return "deleted-records";
    }

    @GetMapping("/vehicle-documents")
    public String vehicleDocuments() {
        return "vehicle-documents";
    }

    @GetMapping("/reminders")
    public String reminders() {
        return "reminders";
    }
}
