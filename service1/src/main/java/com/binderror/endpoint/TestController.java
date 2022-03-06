package com.binderror.endpoint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/api")
public class TestController {

    @RolesAllowed("viewer")
    @RequestMapping(value = "/viewer", method = RequestMethod.GET)
    public ResponseEntity<String> getViewer() {
        return ResponseEntity.ok("Hello Viewer");
    }

    @RolesAllowed("editor")
    @RequestMapping(value = "/editor", method = RequestMethod.GET)
    public ResponseEntity<String> getEditor() {
        return ResponseEntity.ok("Hello Editor");
    }

    @RolesAllowed("approver")
    @RequestMapping(value = "/approver", method = RequestMethod.GET)
    public ResponseEntity<String> getApprover() {
        return ResponseEntity.ok("Hello Approver");
    }

}