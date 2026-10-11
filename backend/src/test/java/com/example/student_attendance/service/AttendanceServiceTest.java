package com.example.student_attendance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import com.example.student_attendance.entity.Attendance;
import com.example.student_attendance.entity.AttendanceSessionRequest;
import com.example.student_attendance.entity.Student;
import com.example.student_attendance.repository.AttendanceRepository;
import com.example.student_attendance.repository.StudentRepository;

import static org.mockito.Mockito.mock;

class AttendanceServiceTest {

    private AttendanceRepository attendanceRepository;
    private StudentRepository studentRepository;
    private AttendanceService service;

    @BeforeEach
    void setUp() {
        attendanceRepository = mock(AttendanceRepository.class);
        studentRepository = mock(StudentRepository.class);
        service = new AttendanceService(attendanceRepository, studentRepository);
    }

    @Test
    void sessionsWithSameDateAndSubjectButDifferentTimesHaveDifferentKeys() {
        when(attendanceRepository.findBySessionKey(anyString())).thenReturn(List.of());

        service.getAttendanceSession(LocalDate.of(2026, 10, 11), "Java", "CSE", "2", "A",
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        service.getAttendanceSession(LocalDate.of(2026, 10, 11), "java", "cse", "2", "a",
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        verify(attendanceRepository, org.mockito.Mockito.times(2)).findBySessionKey(keys.capture());
        assertNotEquals(keys.getAllValues().get(0), keys.getAllValues().get(1));
        assertEquals(64, keys.getAllValues().get(0).length());
    }

    @Test
    void savingAnExistingSessionUpdatesItsStudentRecord() {
        Student student = student(7L);
        Attendance existing = new Attendance(7L, LocalDate.of(2026, 10, 11), "Java", "Present");
        existing.setId(44L);
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(attendanceRepository.findBySessionKey(anyString())).thenReturn(List.of(existing));
        when(attendanceRepository.saveAll(org.mockito.ArgumentMatchers.<Attendance>anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Attendance> saved = service.saveAttendanceSession(request("Absent", 7L));

        assertEquals(1, saved.size());
        assertEquals(44L, saved.get(0).getId());
        assertEquals("Absent", saved.get(0).getStatus());
        assertEquals("CSE", saved.get(0).getDepartment());
        assertEquals("A", saved.get(0).getSection());
    }

    @Test
    void duplicateStudentRowsAreRejectedBeforeSaving() {
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student(7L)));
        AttendanceSessionRequest request = request("Present", 7L);
        request.getStudents().add(studentStatus(7L, "Absent"));

        assertThrows(ResponseStatusException.class, () -> service.saveAttendanceSession(request));
        verify(attendanceRepository, never()).saveAll(org.mockito.ArgumentMatchers.<Attendance>anyList());
    }

    private AttendanceSessionRequest request(String status, Long studentId) {
        AttendanceSessionRequest request = new AttendanceSessionRequest();
        request.setDate(LocalDate.of(2026, 10, 11));
        request.setSubject("Java");
        request.setDepartment("CSE");
        request.setYear("2");
        request.setSection("A");
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(10, 0));
        request.setStudents(new java.util.ArrayList<>(List.of(studentStatus(studentId, status))));
        return request;
    }

    private AttendanceSessionRequest.StudentStatus studentStatus(Long id, String status) {
        AttendanceSessionRequest.StudentStatus entry = new AttendanceSessionRequest.StudentStatus();
        entry.setStudentId(id);
        entry.setStatus(status);
        return entry;
    }

    private Student student(Long id) {
        Student student = new Student();
        student.setId(id);
        student.setDepartment("CSE");
        student.setYear("2");
        student.setSection("A");
        return student;
    }
}
