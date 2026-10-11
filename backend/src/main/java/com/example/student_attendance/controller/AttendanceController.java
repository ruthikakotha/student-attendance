package com.example.student_attendance.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.student_attendance.entity.Attendance;
import com.example.student_attendance.entity.AttendanceSessionRequest;
import com.example.student_attendance.service.AttendanceService;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
})
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    private boolean isTeacher(Authentication auth) {
        return auth != null &&
                auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
    }

    @GetMapping
    public ResponseEntity<?> getAllAttendance(Authentication auth) {
        if (!isTeacher(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(attendanceService.getAllAttendance());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAttendanceById(
            @PathVariable Long id,
            Authentication auth) {

        if (!isTeacher(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return attendanceService.getAttendanceById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getAttendanceByStudent(
            @PathVariable Long studentId,
            Authentication auth) {

        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (isTeacher(auth)) {
            return ResponseEntity.ok(
                    attendanceService.getAttendanceByStudent(studentId));
        }

        if (!auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Long authenticatedStudentId = Long.valueOf(auth.getName());

            if (!authenticatedStudentId.equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (NumberFormatException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                attendanceService.getAttendanceByStudent(studentId));
    }

    @GetMapping("/date-subject")
    public ResponseEntity<?> getAttendanceByDateAndSubject(
            @RequestParam LocalDate date,
            @RequestParam String subject,
            Authentication auth) {

        if (!isTeacher(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                attendanceService.getAttendanceByDateAndSubject(date, subject));
    }

    @GetMapping("/session")
    public ResponseEntity<?> getAttendanceSession(
            @RequestParam LocalDate date,
            @RequestParam String subject,
            @RequestParam String department,
            @RequestParam String year,
            @RequestParam String section,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime,
            Authentication auth) {

        if (!isTeacher(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                attendanceService.getAttendanceSession(
                        date, subject, department, year, section,
                        startTime, endTime));
    }

    @PutMapping("/session")
    public ResponseEntity<?> saveAttendanceSession(
            @RequestBody AttendanceSessionRequest request,
            Authentication auth) {

        if (!isTeacher(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return ResponseEntity.ok(
                    attendanceService.saveAttendanceSession(request));
        } catch (DataIntegrityViolationException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This student already has attendance for the selected session.");
        }
    }

    @PostMapping
    public ResponseEntity<?> addAttendance(
            @RequestBody Attendance attendance,
            Authentication auth) {

        if (!isTeacher(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                attendanceService.addAttendance(attendance));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAttendance(
            @PathVariable Long id,
            @RequestBody Attendance attendance,
            Authentication auth) {

        if (!isTeacher(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Attendance updated = attendanceService.updateAttendance(id, attendance);

        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttendance(
            @PathVariable Long id,
            Authentication auth) {

        if (!isTeacher(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }
}