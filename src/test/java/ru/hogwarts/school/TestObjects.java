package ru.hogwarts.school;

import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import java.util.List;
import java.util.UUID;

public class TestObjects {


    private final List<Student> studentTestList = List.of(
            new Student("Ivan", 15)
            , new Student("Tom", 19)
            , new Student("Alice", 30)
            , new Student("Ivan", 24)
            , new Student("Frank", 14)
            , new Student("Peter", 26)
            , new Student("Sonya", 20)
            , new Student("Katya", 19)
            , new Student("John", 16)
            , new Student("Adolf", 14)

    );

    private final List<Faculty> facultyTestList = List.of(
            new Faculty("Geography", "Orange")
            , new Faculty("Math", "Blue")
            , new Faculty("Physics", "Red")
            , new Faculty("Biology", "Green")
            , new Faculty("Chemistry", "Purple")
            , new Faculty("Biophysics", "Black")
    );

    private final String stringUUID = UUID.randomUUID().toString();
    public String getTestSubstring() {
        return "test" + stringUUID;
    }

    public TestObjects(){
        studentTestList.forEach(s -> s.setName(s.getName()+getTestSubstring()));
        facultyTestList.forEach(f -> f.setName(f.getName()+getTestSubstring()));
    }

    public List<Student> getStudentList() {
        return studentTestList;
    }

    public List<Faculty> getFacultyList() {
        return facultyTestList;
    }

    public Student getGhostStudent() {
        return new Student(getTestSubstring() + "Ghost", 25);
    }

    public Faculty getGhostFaculty() {
        return new Faculty(getTestSubstring() + "GhostObject", "Red");
    }


}
