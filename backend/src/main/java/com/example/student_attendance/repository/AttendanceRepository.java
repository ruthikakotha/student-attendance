package com.example.student_attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_attendance.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentId(Long studentId);

    List<Attendance> findByDateAndSubject(
            LocalDate date,
            String subject
    );

    List<Attendance> findBySessionKey(String sessionKey);
}
