package ru.hogwarts.school.service;

import ch.qos.logback.classic.spi.LoggerContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.exception.EntityNotFoundException;
import ru.hogwarts.school.exception.EntityNotFoundRuntimeException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;


@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    @Autowired
    public StudentService(StudentRepository studentRepository, FacultyRepository facultyRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    public List<Student> getStudentList() {
        logger.info("Was invoked method getStudentList()");
        return studentRepository.findAll();
    }

    public long getStudentListSize() {
        logger.info("Was invoked method getStudentListSize()");
        return studentRepository.getStudentListSize();
    }

    //До 2 цифр после запятой
    public float getAverageAge() {
        Float averageAge = studentRepository.getAverageAge();
        logger.info("Was invoked method getAverageAge(), Float averageAge = {}", averageAge);
        if (averageAge == null) {
            return 0f;
        } else {
            return (int) (averageAge * 100f) / 100f;
        }
    }

    public Collection<Student> getLastFiveStudents() {
        logger.info("Was invoked method getLastFiveStudents()");
        return studentRepository.findLastFiveStudents();
    }

    public Collection<Student> getStudentsByAgeRange(int min, int max) {
        logger.info("Was invoked method getStudentsByAgeRange(int min = {}, int max = {})", min, max);
        if (min > max) {
            logger.error("Attempt to get students by age range where min = {} larger than max = {}", min, max);
            return null;
        }
        return studentRepository.findByAgeBetween(min, max);
    }

    public ResponseEntity<Faculty> getFacultyById(long id) {
        logger.info("Was invoked method getFacultyById(long id = {})", id);
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            logger.error("Attempt to get faculty of non-existent student, passed student id = {}", id);
            return ResponseEntity.notFound().build();
        }
        Faculty faculty = student.getFaculty();
        if (faculty == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(faculty);

    }

    public Student getStudentById(long id) {
        logger.info("Was invoked method getStudentById(long id = {})", id);
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            logger.error("Attempt to get non-existent student, passed student id = {}", id);
        }
        return student;
    }

    public Student save(Student student) {
        logger.info("Was invoked method save(Student student = {})", student);
        Faculty studentFaculty = student.getFaculty();
        if (studentFaculty != null) {
            long facultyId = studentFaculty.getId();
            Faculty facultyInDataBase = facultyRepository.findById(facultyId).orElse(null);
            if (facultyInDataBase == null) {
                logger.error("Attempt to add a student with a non-existent faculty, passed faculty ID = {}", facultyId);
                throw new EntityNotFoundRuntimeException("The faculty ID of the student being added does not exist in the database.");
            } else {
                student.setFaculty(facultyInDataBase);
            }
        }
        return studentRepository.save(student);

    }

    public Boolean deleteStudent(long id) {
        logger.info("Was invoked method deleteStudent(long id = {})", id);
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return true;
        } else {
            logger.error("Attempt to delete non-existent student, passed student id = {}", id);
            return false;
        }
    }

    public Student updateStudent(Student student) {
        logger.info("Was invoked method updateStudent(Student student = {})", student);
        if (!studentRepository.existsById(student.getId())) {
            logger.error("Attempt to update non-existent student, passed student id = {}", student.getId());
            return null;
        }
        Faculty studentFaculty = student.getFaculty();
        if (studentFaculty != null && !facultyRepository.existsById(studentFaculty.getId())) {
            logger.error("Attempt to update student with non-existent faculty, passed faculty ID = {}", studentFaculty.getId());
            return null;
        }
        return studentRepository.save(student);
    }

    public boolean addFacultyForStudentById(long student_id, long faculty_id) {
        logger.info("Was invoked method addFacultyForStudentById(long student_id = {}, long faculty_id = {})", student_id, faculty_id);
        Student student = studentRepository.findById(student_id).orElse(null);
        Faculty faculty = facultyRepository.findById(faculty_id).orElse(null);
        if (student == null) {
            logger.error("Attempt to add faculty for non-existent student, passed student id = {}", student_id);
            return false;
        } else if (faculty == null) {
            logger.error("Attempt to add non-existent faculty for student, passed faculty ID = {}", faculty_id);
            return false;
        } else {
            studentRepository.updateFacultyById(faculty, student_id);
            return true;
        }

    }


    public void deleteByNameContains(String substring) {
        logger.info("Was invoked method deleteByNameContains(String substring = {})", substring);
        studentRepository.deleteByNameContains(substring);
    }
}
