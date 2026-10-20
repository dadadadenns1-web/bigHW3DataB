package ru.hogwarts.school.controller;

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
import org.springframework.web.util.UriComponentsBuilder;
import ru.hogwarts.school.TestObjects;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;
import ru.hogwarts.school.service.StudentService;

import java.net.URI;
import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FacultyControllerTestRestTemplate {

    @LocalServerPort
    private int port;

    private final FacultyController facultyController;
    private final FacultyService facultyService;
    private final TestRestTemplate testRestTemplate;
    private final StudentService studentService;

    @Autowired
    public FacultyControllerTestRestTemplate(FacultyController facultyController, FacultyService facultyService, TestRestTemplate testRestTemplate, StudentService studentService) {
        this.facultyController = facultyController;
        this.facultyService = facultyService;
        this.testRestTemplate = testRestTemplate;
        this.studentService = studentService;
    }

    private TestObjects testObjects;
    private List<Faculty> testFaculties;
    private List<Student> testStudents;
    private long idOfDeletedFaculty;


    private String getFacultyURL() {
        return "http://localhost:" + port + "/faculty";
    }

    static <T> void assertStatusCode(ResponseEntity<T> response, HttpStatusCode httpStatusCode) {
        Assertions.assertEquals(httpStatusCode, response.getStatusCode(), "An error occurred, status " + response.getStatusCode());
    }

    @BeforeAll
    void beforeAll() {
        Assert.notNull(facultyController, "facultyController is not initialized");
        Assert.notNull(facultyService, "facultyService is not initialized");
        Assert.notNull(studentService, "studentService is not initialized");
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

        //id факультета вне базы данных
        idOfDeletedFaculty = facultyService.save(testObjects.getGhostFaculty()).getId();
        facultyService.deleteFaculty(idOfDeletedFaculty);

        //Студенты будут добавлены в первые 3 факультета
        Assert.isTrue(testFaculties.size() > 3, "Requires more than 3 faculties");
        Assert.isTrue(testStudents.size() > 5, "Requires more than 5 students in testList");

        int counter = 0;
        for (Student studentInTestStudents : testStudents) {
            int facultyNumberInList = counter % 3;
            Faculty faculty = testFaculties.get(facultyNumberInList);
            Student studentInDataBase = studentService.save(studentInTestStudents);

            studentService.addFacultyForStudentById(studentInDataBase.getId(), faculty.getId());

            //Обновление списка студентов у факультета
            faculty.getAllStudents().add(studentInDataBase);

            //Обновление факультета и id студентов
            studentInTestStudents.setFaculty(faculty);
            studentInTestStudents.setId(studentInDataBase.getId());

            counter++;
        }
    }

    @AfterEach
    void cleanUp() {
        studentService.deleteByNameContains(testObjects.getTestSubstring());
        facultyService.deleteByNameContains(testObjects.getTestSubstring());
    }

    @Test
    void addFacultyTest() {
        Faculty facultyToAdd = testObjects.getGhostFaculty();
        ResponseEntity<Faculty> response = this.testRestTemplate.postForEntity(getFacultyURL(), facultyToAdd, Faculty.class);
        assertStatusCode(response, HttpStatus.OK);

        Assert.notNull(response.getBody(), "response body is null");

        Faculty facultyInDataBase = facultyService.getFacultyById(response.getBody().getId());
        Assert.isTrue(facultyInDataBase.getName().equals(facultyToAdd.getName()), "faculty mismatch");
    }

    @Test
    void deleteFaculty() {
        for (Faculty faculty : testFaculties) {
            ResponseEntity<Void> response = this.testRestTemplate.exchange(getFacultyURL() + "/" + faculty.getId(), HttpMethod.DELETE, new HttpEntity<>(null, new HttpHeaders()), Void.class);
            assertStatusCode(response, HttpStatus.OK);
            if (facultyService.getFacultyById(faculty.getId()) != null) {
                throw new AssertionFailure("Faculties were not properly deleted by controller method");
            }
            //Проверка что у студентов отвязались факультеты
            Collection<Student> studentsInTestList = faculty.getAllStudents();
            Assert.isTrue(studentsInTestList.stream().map(s -> studentService.getStudentById(s.getId())).allMatch(s -> s.getFaculty() == null), "Faculties were not detached from students");
        }


        ResponseEntity<Void> response = this.testRestTemplate.exchange(getFacultyURL() + "/" + idOfDeletedFaculty, HttpMethod.DELETE, new HttpEntity<>(null, new HttpHeaders()), void.class);
        assertStatusCode(response, HttpStatus.NOT_FOUND);
    }

    @Test
    void getFacultyCollectionTest() {
        ResponseEntity<Faculty[]> response = this.testRestTemplate.getForEntity(getFacultyURL(), Faculty[].class);
        assertStatusCode(response, HttpStatus.OK);

        if (response.getBody() == null) {
            throw new AssertionFailure("response body is null");
        }

        Collection<Faculty> responseBody = Arrays.asList(response.getBody());
        Assert.isTrue(facultyService.getFacultyCollection().containsAll(responseBody)
                        && responseBody.size() == facultyService.getFacultyCollection().size()
                , "Collections mismatch");
    }

    @Test
    void getFacultyByNameOrColor() {
        Assert.isTrue(testFaculties.size() > 2, "Required more than 2 faculties");
        String nameToSearch = "bio";
        String colorToSearch = "ed";
        Assert.isTrue(testFaculties.stream().anyMatch(f -> f.getName().toLowerCase(Locale.ROOT).contains(nameToSearch.toLowerCase(Locale.ROOT))), "Required substring " + nameToSearch + " in testFaculties names");
        Assert.isTrue(testFaculties.stream().anyMatch(f -> f.getColor().toLowerCase(Locale.ROOT).contains(colorToSearch.toLowerCase(Locale.ROOT))), "Required substring " + colorToSearch + " Faculty in testFaculties");

        getFacultyTestWithParameters(nameToSearch, null);
        getFacultyTestWithParameters(null, colorToSearch);
        getFacultyTestWithParameters(nameToSearch, colorToSearch);
        getFacultyTestWithParameters(null, null);

        //Blank test
        ResponseEntity<Faculty[]> response = this.testRestTemplate.getForEntity(getFacultyURL() + "/search?name=" + " " + "&color=" + " ", Faculty[].class);
        assertStatusCode(response, HttpStatus.BAD_REQUEST);
        response = this.testRestTemplate.getForEntity(getFacultyURL() + "/search?color=" + " ", Faculty[].class);
        assertStatusCode(response, HttpStatus.BAD_REQUEST);
        response = this.testRestTemplate.getForEntity(getFacultyURL() + "/search?name=123&color= ", Faculty[].class);
        assertStatusCode(response, HttpStatus.BAD_REQUEST);

    }

    void getFacultyTestWithParameters(String nameToSearch, String colorToSearch) {
        Collection<Faculty> facultiesFiltered = facultyService.getFacultyCollection().stream().filter(f ->
                (nameToSearch != null && f.getName().toLowerCase(Locale.ROOT).contains(nameToSearch.toLowerCase(Locale.ROOT)))
                        || (colorToSearch != null && f.getColor().toLowerCase(Locale.ROOT).contains(colorToSearch.toLowerCase(Locale.ROOT)))).toList();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getFacultyURL()).path("/search");
        if (nameToSearch != null) {
            builder.queryParam("name", nameToSearch);
        }
        if (colorToSearch != null) {
            builder.queryParam("color", colorToSearch);
        }
        URI uri = builder.build().encode().toUri();

        ResponseEntity<Faculty[]> response = this.testRestTemplate.getForEntity(uri, Faculty[].class);
        assertStatusCode(response, HttpStatus.OK);
        if (response.getBody() == null) {
            throw new AssertionFailure("response body is null");
        }
        Collection<Faculty> responseBody = Arrays.asList(response.getBody());
        Assert.isTrue(responseBody.containsAll(facultiesFiltered) && responseBody.size() == facultiesFiltered.size(), "Filtering mismatch");


    }

    @Test
    void getAllStudentsById() {
        Assert.isTrue(testFaculties.get(0).getAllStudents().size() > 2, "First faculty without students");
        Faculty facultyToTest = testFaculties.get(0);
        ResponseEntity<Student[]> response = this.testRestTemplate.getForEntity(getFacultyURL() + "/" + facultyToTest.getId() + "/students", Student[].class);
        assertStatusCode(response, HttpStatus.OK);
        if (response.getBody() == null) {
            throw new AssertionFailure("response body is null");
        }
        Faculty facultyInDataBase = facultyService.getFacultyById(facultyToTest.getId());
        Collection<Student> responseBody = Arrays.asList(response.getBody());
        Assert.isTrue(facultyInDataBase.getAllStudents().containsAll(responseBody)
                && facultyInDataBase.getAllStudents().size() == responseBody.size()
                && !responseBody.isEmpty(), "student lists are mismatching");


    }

    @Test
    void getFacultyByIdTest() {
        Faculty facultyToTest = testFaculties.get(0);
        ResponseEntity<Faculty> response = this.testRestTemplate.getForEntity(getFacultyURL() + "/" + facultyToTest.getId(), Faculty.class);
        assertStatusCode(response, HttpStatus.OK);
        if (response.getBody() == null) {
            throw new AssertionFailure("response body is null");
        }
        Assert.isTrue(Objects.equals(response.getBody(), facultyService.getFacultyById(facultyToTest.getId())), "Faculties mismatching");

        response = this.testRestTemplate.getForEntity(getFacultyURL() + "/" + idOfDeletedFaculty, Faculty.class);
        assertStatusCode(response, HttpStatus.NOT_FOUND);
    }

    @Test
    void updateFacultyTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Faculty facultyToUpdate = testFaculties.get(0);
        String stringUUID = UUID.randomUUID().toString();

        Faculty facultyWithChangedName = new Faculty(facultyToUpdate.getName() + stringUUID, facultyToUpdate.getColor());
        facultyWithChangedName.setId(facultyToUpdate.getId());
        facultyWithChangedName.getAllStudents().addAll(facultyToUpdate.getAllStudents());

        ResponseEntity<Faculty> response = this.testRestTemplate.exchange(getFacultyURL(), HttpMethod.PUT, new HttpEntity<>(facultyWithChangedName, headers), Faculty.class);
        assertStatusCode(response, HttpStatus.OK);

        if (response.getBody() == null) {
            throw new AssertionFailure("response body is null");
        }
        Faculty facultyInDataBase = facultyService.getFacultyById(response.getBody().getId());

        Assert.isTrue(Objects.equals(facultyInDataBase, facultyWithChangedName), "PutList and dataBase list mismatch");

        //Проверка, что ничего не поменялось кроме имени факультета
        Assert.isTrue(facultyInDataBase.getName().contains(stringUUID), "Name was not changed");
        Assert.isTrue(facultyInDataBase.getId() == facultyToUpdate.getId()
                        && Objects.equals(facultyInDataBase.getColor(), facultyToUpdate.getColor())
                        && facultyInDataBase.getAllStudents().containsAll(facultyToUpdate.getAllStudents())
                        && facultyInDataBase.getAllStudents().size() == facultyToUpdate.getAllStudents().size()
                , "Unnecessary fields were modified");
    }

    @Test
    void updateFacultyNotFoundTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Faculty correctFaculty = testFaculties.get(0);
        Faculty facultyWithWrongId = new Faculty(correctFaculty.getName(), correctFaculty.getColor(), idOfDeletedFaculty, correctFaculty.getAllStudents());

        ResponseEntity<Faculty> response = this.testRestTemplate.exchange(getFacultyURL(), HttpMethod.PUT, new HttpEntity<>(facultyWithWrongId, headers), Faculty.class);
        assertStatusCode(response, HttpStatus.NOT_FOUND);
    }

    @Test
    void studentListUpdateTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Faculty facultyToUpdate = testFaculties.get(0);
        Collection<Student> newStudentList = new ArrayList<>();
        Student studentInList = testStudents.get(0);
        newStudentList.add(studentInList);

        Assert.isTrue(!(facultyToUpdate.getAllStudents().containsAll(newStudentList)
                && facultyToUpdate.getAllStudents().size() == newStudentList.size()), "lists are equals");

        //Создание копии, но с измененным листом
        Faculty facultyWithChangedList = new Faculty(facultyToUpdate.getName(), facultyToUpdate.getColor(), facultyToUpdate.getId(), newStudentList);

        ResponseEntity<Faculty> response = this.testRestTemplate.exchange(getFacultyURL(), HttpMethod.PUT, new HttpEntity<>(facultyWithChangedList, headers), Faculty.class);
        assertStatusCode(response, HttpStatus.OK);

        if (response.getBody() == null) {
            throw new AssertionFailure("response body is null");
        }
        Faculty facultyInDataBase = facultyService.getFacultyById(response.getBody().getId());
        Assert.isTrue(Objects.equals(facultyInDataBase, facultyWithChangedList), "PutList and dataBase list mismatch");

        //Проверка, что у студента в базе данных также поменялся факультет на отданный в методе put
        Assert.isTrue(Objects.equals(studentService.getStudentById(studentInList.getId()).getFaculty(), facultyWithChangedList), "Faculties are mismatching");

        //Проверка, что ничего не поменялось кроме листа студентов
        Assert.isTrue(facultyInDataBase.getId() == facultyToUpdate.getId()
                        && Objects.equals(facultyInDataBase.getColor(), facultyToUpdate.getColor())
                        && Objects.equals(facultyInDataBase.getName(), facultyToUpdate.getName())
                , "Unnecessary fields were modified");
    }
}
