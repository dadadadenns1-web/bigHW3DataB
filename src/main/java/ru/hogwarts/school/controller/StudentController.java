package ru.hogwarts.school.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.Collection;

@RestController
@RequestMapping("student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping()
    Collection<Student> getStudentCollection() {
        return studentService.getStudentCollection();
    }

    @GetMapping("/age")
    ResponseEntity<Collection<Student>> searchStudentsByAgeRange(@RequestParam int min,@RequestParam int max){
        Collection<Student> studentsByAge = studentService.getStudentsByAgeRange(min,max);
        if(studentsByAge == null){
            return ResponseEntity.badRequest().build();
        }else{
            return ResponseEntity.ok(studentsByAge);
        }
    }

    @GetMapping({"{id}/faculty"})
    ResponseEntity<Faculty> getStudentFacultyNameById(@PathVariable long id){
        return ResponseEntity.ofNullable(studentService.getFacultyById(id));
    }

    @GetMapping("/{id}")
    ResponseEntity<Student> getStudentById(@PathVariable long id) {
        return ResponseEntity.ofNullable(studentService.getStudentById(id));
    }

    @PostMapping()
    Student addNewStudent(@RequestBody Student student) {
        return studentService.addNewStudent(student);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteStudent(@PathVariable long id) {
        if (studentService.deleteStudent(id) == true) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping()
    ResponseEntity<Student> updateStudent(@RequestBody Student studentToUpdate) {
        Student updatedStudent = studentService.updateStudent(studentToUpdate);
        if (updatedStudent != null) {
            return ResponseEntity.ok(updatedStudent);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
