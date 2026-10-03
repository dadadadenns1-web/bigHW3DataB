package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;


@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Collection<Student> getStudentCollection() {
        return studentRepository.findAll();
    }

    public Collection<Student> getStudentsByAgeRange(int min, int max) {
        if (min > max) {
            return null;
        }
        return studentRepository.findByAgeBetween(min, max);
    }

    public Faculty getFacultyById(long id) {
        if (studentRepository.existsById(id)) {
            return studentRepository.getReferenceById(id).getFaculty();
        } else {
            return null;
        }
    }

    public Student getStudentById(long id) {
        return studentRepository.findById(id).orElse(null);
    }

    public Student addNewStudent(Student student) {
        return studentRepository.save(student);
    }

    public Boolean deleteStudent(long id) {
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Student updateStudent(Student student) {
        if (studentRepository.existsById(student.getId())) {
            return studentRepository.save(student);
        } else {
            return null;
        }
    }


}
