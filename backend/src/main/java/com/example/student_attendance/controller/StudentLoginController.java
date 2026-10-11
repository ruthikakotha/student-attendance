package com.example.student_attendance.controller;

import com.example.student_attendance.JwtService;
import com.example.student_attendance.entity.Student;
import com.example.student_attendance.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student-login")
@CrossOrigin(origins = {
                "http://localhost:5173",
                "http://127.0.0.1:5173"
})
public class StudentLoginController {

        private final StudentRepository studentRepository;
        private final BCryptPasswordEncoder encoder;
        private final JwtService jwtService;

        public StudentLoginController(
                        StudentRepository studentRepository,
                        BCryptPasswordEncoder encoder,
                        JwtService jwtService) {
                this.studentRepository = studentRepository;
                this.encoder = encoder;
                this.jwtService = jwtService;
        }

        @PostMapping
        public ResponseEntity<?> login(@RequestBody Map<String, String> data) {

                String rollNo = data.get("rollNo");
                String password = data.get("password");

                if (rollNo == null || password == null) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("message",
                                                        "Enter roll number and password"));
                }

                Student student = studentRepository.findByRollNo(rollNo)
                                .orElse(null);

                if (student == null || student.getPassword() == null
                                || !encoder.matches(password, student.getPassword())) {
                        return ResponseEntity.status(401)
                                        .body(Map.of("message",
                                                        "Invalid roll number or password"));
                }

                String token = jwtService.generateToken(
                                String.valueOf(student.getId()), "STUDENT");

                return ResponseEntity.ok(Map.of(
                                "token", token,
                                "role", "STUDENT",
                                "id", student.getId(),
                                "rollNo", student.getRollNo(),
                                "name", student.getName(),
                                "department", student.getDepartment(),
                                "year", student.getYear(),
                                "section", student.getSection()));
        }
}