package ru.hogwarts.school.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(StudentController.class)
public class StudentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    @Test
    void addStudentTest() throws Exception {
        JSONObject studentObject = new JSONObject();
        Student expectedStudent = new Student("Ivan", 25, 3);

        studentObject.put("name", expectedStudent.getName());
        studentObject.put("age", expectedStudent.getAge());

        when(studentService.save(any(Student.class))).thenReturn(expectedStudent);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/student")
                        .content(studentObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(expectedStudent.getName()))
                .andExpect(jsonPath("$.age").value(expectedStudent.getAge()))
                .andExpect(jsonPath("$.faculty").value(expectedStudent.getFaculty()));

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentService).save(captor.capture());
        Assert.isTrue(Objects.equals(captor.getValue().getName(), expectedStudent.getName()), "Request body not as expected");
    }

    @Test
    void removeStudentTest() throws Exception {
        long rightId = 5L;
        long falseId = 4L;

        when(studentService.deleteStudent(rightId)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/student/" + rightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()));

        when(studentService.deleteStudent(falseId)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/student/" + falseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
        verify(studentService).deleteStudent(rightId);
        verify(studentService).deleteStudent(falseId);
    }

    @Test
    void putStudentTest() throws Exception {
        JSONObject studentObject = new JSONObject();
        Student expectedStudent = new Student("Ivan", 25, 4);

        studentObject.put("name", expectedStudent.getName());
        studentObject.put("age", expectedStudent.getAge());
        studentObject.put("id", expectedStudent.getId());

        when(studentService.updateStudent(expectedStudent)).thenReturn(expectedStudent);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/student")
                        .content(studentObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.id").value(expectedStudent.getId()))
                .andExpect(jsonPath("$.name").value(expectedStudent.getName()))
                .andExpect(jsonPath("$.age").value(expectedStudent.getAge()))
                .andExpect(jsonPath("$.faculty").value(expectedStudent.getFaculty()));

        when(studentService.updateStudent(expectedStudent)).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/student")
                        .content(studentObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void getStudentTest() throws Exception {
        Student expectedStudent = new Student("Ivan", 25, 5);
        when(studentService.getStudentById(expectedStudent.getId())).thenReturn(expectedStudent);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student/" + expectedStudent.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.id").value(expectedStudent.getId()))
                .andExpect(jsonPath("$.name").value(expectedStudent.getName()))
                .andExpect(jsonPath("$.age").value(expectedStudent.getAge()))
                .andExpect(jsonPath("$.faculty").value(expectedStudent.getFaculty()));

        when(studentService.getStudentById(expectedStudent.getId())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student/" + expectedStudent.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

    }

    @Test
    void getStudentFacultyByIdTest() throws Exception {
        Student expectedStudent = new Student("Ivan", 25, 3L);
        Faculty expectedFaculty = new Faculty("Math", "Red", 5L, new ArrayList<>(List.of(expectedStudent)));
        ResponseEntity<Faculty> response = ResponseEntity.ok(expectedFaculty);

        when(studentService.getFacultyById(expectedStudent.getId())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student/" + expectedStudent.getId() + "/faculty")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.id").value(expectedFaculty.getId()))
                .andExpect(jsonPath("$.name").value(expectedFaculty.getName()));

        response = ResponseEntity.notFound().build();
        when(studentService.getFacultyById(expectedStudent.getId())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student/" + expectedStudent.getId() + "/faculty")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

        response = ResponseEntity.noContent().build();
        when(studentService.getFacultyById(expectedStudent.getId())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student/" + expectedStudent.getId() + "/faculty")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

    }

    @Test
    void getStudentListTest() throws Exception {
        Student expectedStudent = new Student("Ivan", 25, 3L);
        Student expectedStudentTwo = new Student("Sara", 19, 6L);
        List<Student> expectedCollection = new ArrayList<>(List.of(expectedStudent, expectedStudentTwo));

        when(studentService.getStudentList()).thenReturn(expectedCollection);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.size()").value(expectedCollection.size()))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Ivan", "Sara")));

        expectedCollection.clear();
        when(studentService.getStudentList()).thenReturn(expectedCollection);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.size()").value(expectedCollection.size()))
                .andExpect(jsonPath("$[0].name").doesNotExist());
    }

    @Test
    void searchStudentsByAgeRangeTest() throws Exception {
        Student expectedStudent = new Student("Ivan", 25, 3L);
        Student expectedStudentTwo = new Student("Sara", 19, 6L);
        Collection<Student> expectedCollection = List.of(expectedStudent, expectedStudentTwo);
        int minAge = 15;
        int maxAge = 30;

        when(studentService.getStudentsByAgeRange(minAge, maxAge)).thenReturn(expectedCollection);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student/age?min=" + minAge + "&max=" + maxAge)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.size()").value(expectedCollection.size()))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Ivan", "Sara")));

        when(studentService.getStudentsByAgeRange(maxAge, minAge)).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student/age?min=" + maxAge + "&max=" + minAge)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }
}

