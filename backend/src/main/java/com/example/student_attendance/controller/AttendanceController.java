package com.example.student_attendance.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.student_attendance.entity.Attendance;
import com.example.student_attendance.entity.AttendanceSessionRequest;
import com.example.student_attendance.service.AttendanceService;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // Get all attendance records
    @GetMapping
    public List<Attendance> getAllAttendance() {
        return attendanceService.getAllAttendance();
    }

    // Get attendance by ID
    @GetMapping("/{id}")
    public ResponseEntity<Attendance> getAttendanceById(
            @PathVariable Long id) {

        return attendanceService.getAttendanceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get attendance of a particular student
    @GetMapping("/student/{studentId}")
    public List<Attendance> getAttendanceByStudent(
            @PathVariable Long studentId) {

        return attendanceService.getAttendanceByStudent(studentId);
    }

    // Get attendance by date and subject
    @GetMapping("/date-subject")
    public List<Attendance> getAttendanceByDateAndSubject(
            @RequestParam LocalDate date,
            @RequestParam String subject) {

        return attendanceService.getAttendanceByDateAndSubject(
                date,
                subject
        );
    }

    @GetMapping("/session")
    public List<Attendance> getAttendanceSession(
            @RequestParam LocalDate date,
            @RequestParam String subject,
            @RequestParam String department,
            @RequestParam String year,
            @RequestParam String section,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime) {
        return attendanceService.getAttendanceSession(date, subject, department,
                year, section, startTime, endTime);
    }

    @PutMapping("/session")
    public ResponseEntity<?> saveAttendanceSession(
            @RequestBody AttendanceSessionRequest request) {
        try {
            return ResponseEntity.ok(attendanceService.saveAttendanceSession(request));
        } catch (DataIntegrityViolationException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("This student already has attendance for the selected session.");
        }
    }

    // Add attendance
    @PostMapping
    public Attendance addAttendance(
            @RequestBody Attendance attendance) {

        return attendanceService.addAttendance(attendance);
    }

    // Update attendance
    @PutMapping("/{id}")
    public ResponseEntity<Attendance> updateAttendance(
            @PathVariable Long id,
            @RequestBody Attendance attendance) {

        Attendance updatedAttendance =
                attendanceService.updateAttendance(id, attendance);

        if (updatedAttendance == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedAttendance);
    }

    // Delete attendance
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(
            @PathVariable Long id) {

        attendanceService.deleteAttendance(id);

        return ResponseEntity.noContent().build();
    }
}
