package ru.hogwarts.school.controller;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(FacultyController.class)
public class FacultyControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FacultyService facultyService;

    @Test
    void getFacultyByIdTest() throws Exception {
        Faculty expectedFaculty = new Faculty("Math", "Red", 5L, Collections.emptyList());
        when(facultyService.getFacultyById(expectedFaculty.getId())).thenReturn(expectedFaculty);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculty/" + expectedFaculty.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.id").value(expectedFaculty.getId()))
                .andExpect(jsonPath("$.name").value(expectedFaculty.getName()))
                .andExpect(jsonPath("$.color").value(expectedFaculty.getColor()));

        when(facultyService.getFacultyById(expectedFaculty.getId())).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculty/" + expectedFaculty.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void putFacultyTest() throws Exception {

        JSONObject facultyObject = new JSONObject();
        Faculty expectedFaculty = new Faculty("Math", "Red", 5L, Collections.emptyList());

        facultyObject.put("name", expectedFaculty.getName());
        facultyObject.put("color", expectedFaculty.getColor());
        facultyObject.put("id", expectedFaculty.getId());


        when(facultyService.updateFaculty(expectedFaculty)).thenReturn(expectedFaculty);
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/faculty")
                        .content(facultyObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.id").value(expectedFaculty.getId()))
                .andExpect(jsonPath("$.name").value(expectedFaculty.getName()))
                .andExpect(jsonPath("$.color").value(expectedFaculty.getColor()));

        when(facultyService.updateFaculty(expectedFaculty)).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/faculty")
                        .content(facultyObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

    }

    @Test
    void addFacultyTest() throws Exception {
        JSONObject facultyObject = new JSONObject();
        Faculty expectedFaculty = new Faculty("Math", "Red", 5L, Collections.emptyList());

        facultyObject.put("name", expectedFaculty.getName());
        facultyObject.put("color", expectedFaculty.getColor());

        when(facultyService.save(any(Faculty.class))).thenReturn(expectedFaculty);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/faculty")
                        .content(facultyObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(expectedFaculty.getName()))
                .andExpect(jsonPath("$.color").value(expectedFaculty.getColor()));

        ArgumentCaptor<Faculty> captor = ArgumentCaptor.forClass(Faculty.class);
        verify(facultyService).save(captor.capture());
        Assert.isTrue(Objects.equals(captor.getValue().getName(), expectedFaculty.getName()), "Request body not as expected");

    }

    @Test
    void removeFacultyTest() throws Exception {
        long rightId = 5L;
        long falseId = 4L;

        when(facultyService.deleteFaculty(rightId)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/faculty/" + rightId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()));

        when(facultyService.deleteFaculty(falseId)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/faculty/" + falseId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));

        verify(facultyService).deleteFaculty(rightId);
        verify(facultyService).deleteFaculty(falseId);
    }

    @Test
    void getFacultyCollectionTest() throws Exception {
        Faculty expectedFaculty = new Faculty("Math", "Red", 5L, Collections.emptyList());
        Faculty expectedFacultyTwo = new Faculty("Physics", "Blue", 4L, Collections.emptyList());
        Collection<Faculty> facultyCollection = new ArrayList<>(List.of(expectedFaculty, expectedFacultyTwo));

        when(facultyService.getFacultyCollection()).thenReturn(facultyCollection);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculty")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.size()").value(facultyCollection.size()))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Math", "Physics")));

        facultyCollection.clear();
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculty")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.size()").value(facultyCollection.size()))
                .andExpect(jsonPath("$[0].name").doesNotExist());
    }

    @Test
    void getAllStudentsByIdTest() throws Exception {
        Student expectedStudent = new Student("Ivan", 25, 3L);
        Student expectedStudentTwo = new Student("Sara", 19, 6L);
        List<Student> expectedStudentList = new ArrayList<>(List.of(expectedStudent, expectedStudentTwo));
        long facultyId = 5L;

        when(facultyService.getAllStudentsByFacultyId(facultyId)).thenReturn(expectedStudentList);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculty/" + facultyId + "/students")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.size()").value(expectedStudentList.size()))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Ivan", "Sara")));

        when(facultyService.getAllStudentsByFacultyId(facultyId)).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculty/" + facultyId + "/students")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void getFacultyByNameOrColorTest() throws Exception {
        Faculty expectedFaculty = new Faculty("Math", "Red", 5L, Collections.emptyList());
        Faculty expectedFacultyTwo = new Faculty("Physics", "Blue", 4L, Collections.emptyList());
        Collection<Faculty> facultyCollection = new ArrayList<>(List.of(expectedFaculty, expectedFacultyTwo));

        String nameToSearch = "Math";
        String colorToSearch = "Blue";
        when(facultyService.getFacultyByNameOrColor(nameToSearch, colorToSearch)).thenReturn(facultyCollection);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculty/search?name=" + nameToSearch + "&color=" + colorToSearch)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.size()").value(facultyCollection.size()))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Math", "Physics")));

        facultyCollection.clear();
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculty/search?name=" + nameToSearch + "&color=" + colorToSearch)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.size()").value(facultyCollection.size()))
                .andExpect(jsonPath("$[0].name").doesNotExist());

        when(facultyService.getFacultyByNameOrColor(" ", "")).thenReturn(null);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculty/search?name= &color=")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

    }
}
