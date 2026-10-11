package com.example.student_attendance.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AttendanceSessionRequest {

    private LocalDate date;
    private String subject;
    private String department;
    private String year;
    private String section;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<StudentStatus> students;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public List<StudentStatus> getStudents() { return students; }
    public void setStudents(List<StudentStatus> students) { this.students = students; }

    public static class StudentStatus {
        private Long studentId;
        private String status;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
