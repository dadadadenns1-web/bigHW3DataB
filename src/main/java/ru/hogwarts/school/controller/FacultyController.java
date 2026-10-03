package ru.hogwarts.school.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.Collection;

@RestController
@RequestMapping("faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping()
    Collection<Faculty> getFacultyCollection() {
        return facultyService.getFacultyCollection();
    }

    @GetMapping("/search")
    ResponseEntity<Collection<Faculty>> getFacultyByNameOrColor(@RequestParam(required = false) String name, @RequestParam(required = false) String color){
        Collection<Faculty> maybeFaculties = facultyService.getFacultyByNameOrColor(name, color);
        if(maybeFaculties == null){
            return ResponseEntity.badRequest().build();
        }else{
            return ResponseEntity.ok(maybeFaculties);
        }
    }

    @GetMapping({"{id}/students"})
    ResponseEntity<Collection<Student>> getAllStudentsById(@PathVariable long id){
        return ResponseEntity.ofNullable(facultyService.getAllStudentsByFacultyId(id));
    }

    @GetMapping("{id}")
    ResponseEntity<Faculty> getFacultyById(@PathVariable long id) {
        return ResponseEntity.ofNullable(facultyService.getFacultyById(id));
    }

    @PostMapping()
    Faculty addNewFaculty(@RequestBody Faculty faculty) {
        return facultyService.addNewFaculty(faculty);
    }

    @DeleteMapping("{id}")
    ResponseEntity<Void> deleteFaculty(@PathVariable long id) {
        if (facultyService.deleteFaculty(id) == true) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping()
    ResponseEntity<Faculty> updateFaculty(@RequestBody Faculty facultyToUpdate) {
        Faculty updatedFaculty = facultyService.updateFaculty(facultyToUpdate);
        if (updatedFaculty != null) {
            return ResponseEntity.ok(updatedFaculty);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
