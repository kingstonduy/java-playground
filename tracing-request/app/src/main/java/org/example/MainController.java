package org.example;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping("/public/greet")
    public ResponseEntity<String> publicGreet(HttpServletRequest request,
                                              @RequestParam(defaultValue = "Guest") String name) {
        String s = request.getSession().getId();
        return ResponseEntity.status(HttpStatus.OK)
                .header("X-Request-Id", request.getSession().getId())
                .body("Hello, " + name + "!");
    }

    @GetMapping("/greet")
    public ResponseEntity<String> greet(HttpServletRequest request,
                                        @RequestParam(defaultValue = "Guest") String name) {
        String s = request.getSession().getId();
        return ResponseEntity.status(HttpStatus.OK)
                .header("X-Request-Id", request.getSession().getId())
                .body("Hello, " + name + "!");
    }

    @GetMapping("/hello/{name}")
    public ResponseEntity<String> helloPath(HttpServletRequest request,
                                            @PathVariable String name) {
        return ResponseEntity.ok("Hello, " + name + "!");
    }

    @PostMapping("/echo")
    public ResponseEntity<String> echo(HttpServletRequest request,
                                       @RequestBody String body) {
        return ResponseEntity.status(HttpStatus.CREATED) // 201
                .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                .body("You posted: " + body);
    }
}
