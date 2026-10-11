package com.example.student_attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.student_attendance.entity.Attendance;
import com.example.student_attendance.entity.AttendanceSessionRequest;
import com.example.student_attendance.entity.Student;
import com.example.student_attendance.repository.AttendanceRepository;
import com.example.student_attendance.repository.StudentRepository;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public Optional<Attendance> getAttendanceById(Long id) {
        return attendanceRepository.findById(id);
    }

    public Attendance addAttendance(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    public Attendance updateAttendance(Long id, Attendance attendance) {

        Optional<Attendance> existingAttendance =
                attendanceRepository.findById(id);

        if (existingAttendance.isPresent()) {

            Attendance updatedAttendance = existingAttendance.get();

            updatedAttendance.setStudentId(attendance.getStudentId());
            updatedAttendance.setDate(attendance.getDate());
            updatedAttendance.setSubject(attendance.getSubject());
            updatedAttendance.setStatus(attendance.getStatus());

            return attendanceRepository.save(updatedAttendance);
        }

        return null;
    }

    public void deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
    }

    public List<Attendance> getAttendanceByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    public List<Attendance> getAttendanceByDateAndSubject(
            LocalDate date,
            String subject) {

        return attendanceRepository.findByDateAndSubject(
                date,
                subject
        );
    }

    public List<Attendance> getAttendanceSession(
            LocalDate date,
            String subject,
            String department,
            String year,
            String section,
            LocalTime startTime,
            LocalTime endTime) {
        return attendanceRepository.findBySessionKey(sessionKey(
                date, subject, department, year, section, startTime, endTime));
    }

    @Transactional
    public List<Attendance> saveAttendanceSession(
            AttendanceSessionRequest request) {
        validateSession(request);

        String normalizedSubject = clean(request.getSubject());
        String department = clean(request.getDepartment());
        String year = clean(request.getYear());
        String section = clean(request.getSection());
        String key = sessionKey(request.getDate(), normalizedSubject,
                department, year, section, request.getStartTime(), request.getEndTime());

        Map<Long, Attendance> existingByStudent = new HashMap<>();
        attendanceRepository.findBySessionKey(key).forEach(record ->
                existingByStudent.put(record.getStudentId(), record));

        Set<Long> seenStudentIds = new HashSet<>();
        List<Attendance> records = request.getStudents().stream().map(entry -> {
            if (entry == null || entry.getStudentId() == null
                    || !seenStudentIds.add(entry.getStudentId())) {
                throw badRequest("Each student must appear only once in a session.");
            }

            String status = canonicalStatus(entry.getStatus());
            Student student = studentRepository.findById(entry.getStudentId())
                    .orElseThrow(() -> badRequest("A selected student no longer exists."));
            if (!same(student.getDepartment(), department)
                    || !same(student.getYear(), year)
                    || !same(student.getSection(), section)) {
                throw badRequest("A selected student does not belong to this class session.");
            }

            Attendance record = existingByStudent.getOrDefault(
                    entry.getStudentId(), new Attendance());
            record.setStudentId(entry.getStudentId());
            record.setDate(request.getDate());
            record.setSubject(normalizedSubject);
            record.setStatus(status);
            record.setDepartment(department);
            record.setYear(year);
            record.setSection(section);
            record.setStartTime(request.getStartTime());
            record.setEndTime(request.getEndTime());
            record.setSessionKey(key);
            return record;
        }).toList();

        if (records.isEmpty()) {
            throw badRequest("A session must include at least one student.");
        }
        return attendanceRepository.saveAll(records);
    }

    private void validateSession(AttendanceSessionRequest request) {
        if (request == null || request.getDate() == null
                || isBlank(request.getSubject()) || isBlank(request.getDepartment())
                || isBlank(request.getYear()) || isBlank(request.getSection())
                || request.getStartTime() == null || request.getEndTime() == null) {
            throw badRequest("Complete all class session details.");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw badRequest("End time must be later than start time.");
        }
        if (request.getStudents() == null || request.getStudents().isEmpty()) {
            throw badRequest("A session must include at least one student.");
        }
    }

    private String canonicalStatus(String status) {
        if (status == null) {
            throw badRequest("Attendance status is required.");
        }
        return switch (status.trim().toLowerCase(Locale.ROOT)) {
            case "present" -> "Present";
            case "absent" -> "Absent";
            default -> throw badRequest("Status must be Present or Absent.");
        };
    }

    private String sessionKey(LocalDate date, String subject, String department,
            String year, String section, LocalTime startTime, LocalTime endTime) {
        if (date == null || isBlank(subject) || isBlank(department) || isBlank(year)
                || isBlank(section) || startTime == null || endTime == null) {
            throw badRequest("Complete all class session details.");
        }
        String canonical = String.join("|", date.toString(), clean(subject).toLowerCase(Locale.ROOT),
                clean(department).toLowerCase(Locale.ROOT), clean(year).toLowerCase(Locale.ROOT),
                clean(section).toLowerCase(Locale.ROOT), startTime.toString(), endTime.toString());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder key = new StringBuilder(64);
            for (byte value : digest) {
                key.append(String.format("%02x", value));
            }
            return key.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    private boolean same(String left, String right) {
        return clean(left).equalsIgnoreCase(clean(right));
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
