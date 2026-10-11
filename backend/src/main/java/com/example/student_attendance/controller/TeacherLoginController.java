package com.example.student_attendance.controller;

import com.example.student_attendance.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher-login")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
})
public class TeacherLoginController {

    private final JwtService jwtService;

    public TeacherLoginController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> data) {

        String username = System.getenv("TEACHER_USERNAME");
        String password = System.getenv("TEACHER_PASSWORD");

        if (username == null || password == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message",
                            "Teacher login is not configured"));
        }

        if (data.get("username") == null
                || data.get("password") == null
                || !username.equals(data.get("username"))
                || !password.equals(data.get("password"))) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message",
                            "Invalid teacher credentials"));
        }

        String token = jwtService.generateToken(username, "TEACHER");

        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", "TEACHER"));
    }
}