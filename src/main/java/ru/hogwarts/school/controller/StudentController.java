package ru.hogwarts.school.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/print/parallel")
    void printParallel(){studentService.printParallel();
    }

    @GetMapping("/print-synchronized")
    void printSynchronized(){
        studentService.printSynchronized();
    }


    @GetMapping("/sort/startsWith/A")//Латинская
    List<String> getStudentListNamesBeginWithLetterA(){
       return studentService.getStudentList().stream()
               .map(student -> student.getName().toUpperCase(Locale.ROOT))
               .filter(upperCaseName -> upperCaseName.startsWith("A")) //Латинская
               .sorted().toList(); //Лучше не использовать .parallel из-за sorted
    }

    @GetMapping("/averageAgeByStreamApi")
    double getAverageAgeStreamApi(){
        return studentService.getStudentList().stream().parallel()
                .mapToInt(Student::getAge)
                .average().orElse(0d);// суммирование идет параллельно и в слиянии, деление после.
    }

    @GetMapping()
    List<Student> getStudentList() {
        return studentService.getStudentList();
    }

    @GetMapping("/age")
    ResponseEntity<Collection<Student>> searchStudentsByAgeRange(@RequestParam int min, @RequestParam int max) {
        Collection<Student> studentsByAge = studentService.getStudentsByAgeRange(min, max);
        if (studentsByAge == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(studentsByAge);
        }
    }

    @GetMapping("/numberOfStudents")
    long getStudentListSize() {
        return studentService.getStudentListSize();
    }

    @GetMapping("/averageAge")
    float getAverageAge() {
        return studentService.getAverageAge();
    }

    @GetMapping("/lastFiveStudents")
    Collection<Student> getLastFiveStudents() {
        return studentService.getLastFiveStudents();
    }

    @GetMapping({"{id}/faculty"})
    ResponseEntity<Faculty> getStudentFacultyById(@PathVariable long id) {
        return studentService.getFacultyById(id);
    }

    @GetMapping("/{id}")
    ResponseEntity<Student> getStudentById(@PathVariable long id) {
        return ResponseEntity.ofNullable(studentService.getStudentById(id));
    }

    @PostMapping()
    Student addNewStudent(@RequestBody Student student) {
        return studentService.save(student);
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
            return ResponseEntity.notFound().build();
        }
    }



}
