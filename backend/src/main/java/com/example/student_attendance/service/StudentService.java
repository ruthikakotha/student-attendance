package com.example.student_attendance.service;

import com.example.student_attendance.entity.Student;
import com.example.student_attendance.repository.StudentRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Student addStudent(Student student) {
        String password = student.getPassword();

        if (password == null || password.isBlank()) {
            password = student.getRollNo();
        }

        student.setPassword(encoder.encode(password));
        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, Student student) {
        Optional<Student> result = studentRepository.findById(id);

        if (result.isEmpty()) {
            return null;
        }

        Student existing = result.get();
        existing.setRollNo(student.getRollNo());
        existing.setName(student.getName());
        existing.setDepartment(student.getDepartment());
        existing.setYear(student.getYear());
        existing.setSection(student.getSection());

        if (student.getPassword() != null &&
                !student.getPassword().isBlank()) {
            existing.setPassword(
                    encoder.encode(student.getPassword()));
        }

        return studentRepository.save(existing);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}