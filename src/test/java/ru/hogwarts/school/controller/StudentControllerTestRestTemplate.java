package ru.hogwarts.school.controller;

import org.assertj.core.util.Arrays;
import org.hibernate.AssertionFailure;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

import ru.hogwarts.school.TestObjects;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;
import ru.hogwarts.school.service.StudentService;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StudentControllerTestRestTemplate {

    @LocalServerPort
    private int port;

    private final StudentService studentService;
    private final StudentController studentController;
    private final TestRestTemplate restTemplate;
    private final FacultyService facultyService;

    @Autowired
    public StudentControllerTestRestTemplate(StudentService studentService, StudentController studentController, TestRestTemplate restTemplate, FacultyService facultyService) {
        this.studentService = studentService;
        this.studentController = studentController;
        this.restTemplate = restTemplate;
        this.facultyService = facultyService;
    }

    private TestObjects testObjects;
    private List<Student> testStudents;
    private List<Faculty> testFaculties;

    private String getStudentURL() {
        return "http://localhost:" + port + "/student";
    }

    private long idOfDeletedStudent;
    private long idOfDeletedFaculty;
    private static final int studentsWithFaculty = 3;

    static <T> void assertStatusCode(ResponseEntity<T> response, HttpStatusCode httpStatusCode) {
        Assertions.assertEquals(httpStatusCode, response.getStatusCode(), "An error occurred, status " + response.getStatusCode());
    }

    @BeforeAll
    void loadTestStudents() {
        Assert.notNull(studentController, "student controller is not initialized");
        Assert.notNull(studentService, "student service is not initialized");
        Assert.notNull(facultyService, "faculty service is not initialized");
    }

    @BeforeEach
    void beforeEach() {
        testObjects = new TestObjects();
        testFaculties = testObjects.getFacultyList();
        testStudents = testObjects.getStudentList();


        //Загрузка факультетов в БД и обновление id у тестовых факультетов с id базой данных
        for (Faculty facultyInTestList : testFaculties) {
            Faculty facultyInDataBase = facultyService.save(facultyInTestList);
            facultyInTestList.setId(facultyInDataBase.getId());
        }

        //Загрузка студентов и обновление id
        for (Student studentInTestList : testStudents) {
            Student studentInDataBase = studentService.save(studentInTestList);
            studentInTestList.setId(studentInDataBase.getId());
        }

        //id факультета вне базы данных
        idOfDeletedFaculty = facultyService.save(testObjects.getGhostFaculty()).getId();
        facultyService.deleteFaculty(idOfDeletedFaculty);

        //id Студента вне базы данных
        idOfDeletedStudent = studentService.save(testObjects.getGhostStudent()).getId();
        studentService.deleteStudent(idOfDeletedStudent);

        //Добавление факультетов к 3 студентам
        for (int i = 0; i < studentsWithFaculty; i++) {
            Student s = testStudents.get(i);
            Faculty f = testFaculties.get(i);
            studentService.addFacultyForStudentById(s.getId(), f.getId());
            //Добавление факультета для студента, добавление студента в факультет
            s.setFaculty(f);
            f.getAllStudents().add(s);
        }
    }

    @AfterEach
    void deleteGarbage() {
        studentService.deleteByNameContains(testObjects.getTestSubstring());
        facultyService.deleteByNameContains(testObjects.getTestSubstring());
    }

    @Test
    void addStudentTest() {
        Student studentToAdd = testObjects.getGhostStudent();
        ResponseEntity<Student> response = this.restTemplate.postForEntity(getStudentURL(), studentToAdd, Student.class);
        assertStatusCode(response, HttpStatus.OK);

        Assert.notNull(response.getBody(), "response body is null");

        Student studentInDataBase = studentService.getStudentById(response.getBody().getId());
        Assert.isTrue(studentInDataBase.getName().equals(studentToAdd.getName()), "student mismatch");
    }

    @Test
    void deleteStudentTest() {
        for (Student student : testStudents) {
            ResponseEntity<Void> response = this.restTemplate.exchange(getStudentURL() + "/" + student.getId(), HttpMethod.DELETE, new HttpEntity<>(null, new HttpHeaders()), Void.class);
            assertStatusCode(response, HttpStatus.OK);
            if (studentService.getStudentById(student.getId()) != null) {
                throw new AssertionFailure("Faculties were not properly deleted by controller method");
            }
        }
        ResponseEntity<Void> response = this.restTemplate.exchange(getStudentURL() + "/" + idOfDeletedStudent, HttpMethod.DELETE, new HttpEntity<>(null, new HttpHeaders()), Void.class);
        assertStatusCode(response, HttpStatus.NOT_FOUND);
    }

    @Test
    void getStudentCollectionTest() {
        ResponseEntity<Student[]> response = this.restTemplate.getForEntity(getStudentURL(), Student[].class);
        assertStatusCode(response, HttpStatus.OK);
        if (response.getBody() == null) {
            throw new AssertionFailure("response body is null");
        }
        Collection<Student> responseBody = java.util.Arrays.asList(response.getBody());

        Assert.isTrue(studentService.getStudentList().containsAll(responseBody)
                        && responseBody.size() == studentService.getStudentList().size()
                , "Collections mismatch");
    }

    @Test
    void getStudentFacultyByIdTest() {
        for (Student student : testStudents) {
            ResponseEntity<Faculty> response = this.restTemplate.getForEntity(getStudentURL() + "/" + student.getId() + "/faculty", Faculty.class);
            Assert.isTrue(Objects.equals(student.getFaculty(), response.getBody()), "Faculties mismatch");
            if (response.getBody() != null) {
                assertStatusCode(response, HttpStatus.OK);
            } else {
                assertStatusCode(response, HttpStatus.NO_CONTENT);
            }
        }
        //Проверка не существующего студента
        ResponseEntity<Faculty> response = this.restTemplate.getForEntity(getStudentURL() + "/" + idOfDeletedStudent + "/faculty", Faculty.class);
        assertStatusCode(response, HttpStatus.NOT_FOUND);
    }

    @Test
    void searchStudentsByAgeRangeTest() {
        int minAge = 15;
        int maxAge = 20;

        Assert.isTrue(testStudents.stream().anyMatch(s -> s.getAge() < minAge || s.getAge() > maxAge), "No students in list to test with not matched age");
        Assert.isTrue(testStudents.stream().anyMatch(s -> s.getAge() >= minAge && s.getAge() <= maxAge), "No students in list to test with matched age");
        Collection<Student> expected = studentService.getStudentList().stream().filter(s -> s.getAge() >= minAge && s.getAge() <= maxAge).toList();


        ResponseEntity<Student[]> response = this.restTemplate.getForEntity(getStudentURL() + "/age?min=" + minAge + "&max=" + maxAge, Student[].class);
        assertStatusCode(response, HttpStatus.OK);

        if (response.getBody() == null) {
            throw new AssertionFailure("response body is null");
        }
        Assert.isTrue(Arrays.asList(response.getBody()).containsAll(expected) && response.getBody().length == expected.size(), "Incorrect sorting response result");

        response = this.restTemplate.getForEntity(getStudentURL() + "/age?min=" + maxAge + "&max=" + minAge, Student[].class);
        assertStatusCode(response, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getStudentByIdTest() {
        Student student = testStudents.get(0);
        ResponseEntity<Student> response = this.restTemplate.getForEntity(getStudentURL() + "/" + student.getId(), Student.class);
        assertStatusCode(response, HttpStatus.OK);


        if (response.getBody() == null) {
            throw new AssertionFailure("response body is null");
        }
        Student studentFromDataBase = studentService.getStudentById(response.getBody().getId());
        Assert.isTrue(Objects.equals(studentFromDataBase.getName(), student.getName()) && studentFromDataBase.getAge() == student.getAge(), "Students mismatched");

        response = this.restTemplate.getForEntity(getStudentURL() + "/" + idOfDeletedStudent, Student.class);
        assertStatusCode(response, HttpStatus.NOT_FOUND);
    }

    @Test
    void updateStudentTest() {
        Student studentToUpdate = testStudents.get(0);

        String stringUUID = UUID.randomUUID().toString();
        Student studentWithChangedName = new Student(studentToUpdate.getName() + stringUUID, studentToUpdate.getAge(), studentToUpdate.getId());
        studentWithChangedName.setFaculty(studentToUpdate.getFaculty());//В конструкторе невозможно указать faculty

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Student> response = this.restTemplate.exchange(getStudentURL(), HttpMethod.PUT, new HttpEntity<>(studentWithChangedName, headers), Student.class);
        assertStatusCode(response, HttpStatus.OK);

        Student studentInDataBase = studentService.getStudentById(studentWithChangedName.getId());
        if (studentInDataBase == null) {
            throw new AssertionFailure("Student is not exist in DataBase");
        } else {
            Assert.isTrue(studentInDataBase.getName().contains(stringUUID), "Name is not changed");
            Assert.isTrue(studentInDataBase.getId() == studentToUpdate.getId()
                            && studentInDataBase.getAge() == studentToUpdate.getAge()
                            && Objects.equals(studentInDataBase.getFaculty(), studentToUpdate.getFaculty())
                    , "Unnecessary fields were modified. student."
                            + "\nstudentInDataBase.getId() = " + studentInDataBase.getId() + " studentToUpdate.getId() = " + studentToUpdate.getId()
                            + "\nstudentInDataBase.getAge() = " + studentInDataBase.getAge() + " studentToUpdate.getAge() = " + studentToUpdate.getAge()
                            + "\nstudentInDataBase.getFaculty() = " + studentInDataBase.getFaculty() + " studentToUpdate.getFaculty() = " + studentToUpdate.getFaculty()
            );
            studentToUpdate.setName(studentInDataBase.getName());
        }
    }

    @Test
    void updateStudentNotFoundTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Student correctStudent = testStudents.get(0);

        Student studentWithWrongId = new Student(correctStudent.getName(), correctStudent.getAge(), idOfDeletedStudent);

        ResponseEntity<Student> response = this.restTemplate.exchange(getStudentURL(), HttpMethod.PUT, new HttpEntity<>(studentWithWrongId, headers), Student.class);
        assertStatusCode(response, HttpStatus.NOT_FOUND);

        studentWithWrongId.setId(correctStudent.getId());
        studentWithWrongId.setFaculty(testObjects.getGhostFaculty());
        studentWithWrongId.getFaculty().setId(idOfDeletedFaculty);

        response = this.restTemplate.exchange(getStudentURL(), HttpMethod.PUT, new HttpEntity<>(studentWithWrongId, headers), Student.class);
        assertStatusCode(response, HttpStatus.NOT_FOUND);

    }
}
