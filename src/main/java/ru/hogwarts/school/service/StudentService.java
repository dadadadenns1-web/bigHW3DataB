package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;
import java.util.List;


@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository, FacultyRepository facultyRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    public List<Student> getStudentList() {
        return studentRepository.findAll();
    }

    public long getStudentListSize(){
        return  studentRepository.getStudentListSize();
    }

    //До 2 цифр после запятой
    public float getAverageAge(){
        Float averageAge = studentRepository.getAverageAge();
        if(averageAge == null){
            return 0f;
        }else {
            return (int) (averageAge * 100f) / 100f;
        }
    }

    public Collection<Student> getLastFiveStudents(){
        return  studentRepository.findLastFiveStudents();
    }

    public Collection<Student> getStudentsByAgeRange(int min, int max) {
        if (min > max) {
            return null;
        }
        return studentRepository.findByAgeBetween(min, max);
    }

    public ResponseEntity<Faculty> getFacultyById(long id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        Faculty faculty = student.getFaculty();
        if (faculty == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(faculty);

    }

    public Student getStudentById(long id) {
        return studentRepository.findById(id).orElse(null);
    }

    public Student save(Student student) {
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
        Faculty studentFaculty = student.getFaculty();
        if (studentFaculty != null && !facultyRepository.existsById(studentFaculty.getId())) {
            return null;
        }
        if (studentRepository.existsById(student.getId())) {
            return studentRepository.save(student);
        } else {
            return null;
        }
    }

    public boolean addFacultyForStudentById(long student_id, long faculty_id) {
        Student student = studentRepository.findById(student_id).orElse(null);
        Faculty faculty = facultyRepository.findById(faculty_id).orElse(null);
        if (student == null || faculty == null) {
            return false;
        }
        studentRepository.updateFacultyById(faculty, student_id);
        return true;
    }


    public void deleteByNameContains(String substring) {
        studentRepository.deleteByNameContains(substring);
    }
}
