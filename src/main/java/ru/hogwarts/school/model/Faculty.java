package ru.hogwarts.school.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Entity
@JsonIgnoreProperties({"allStudents", "hibernateLazyInitializer"})
public class Faculty {

    @Id
    @GeneratedValue
    private long id;
    private String name;
    private String color;

    @OneToMany(mappedBy = "faculty")
    private Collection<Student> studentList = new ArrayList<>();

    public Faculty(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Faculty(String name, String color, long id, Collection<Student> studentList) {
        this(name, color);
        this.id = id;
        this.studentList.addAll(studentList);
    }

    //Без дефолтного конструктора выдаст ошибку.
    public Faculty() {
    }

    public Collection<Student> getAllStudents() {
        return studentList;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }


    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Faculty faculty = (Faculty) object;
        return id == faculty.id && Objects.equals(name, faculty.name) && Objects.equals(color, faculty.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, color);
    }

    @Override
    public String toString() {
        return "Faculty{" +
                "color='" + color + '\'' +
                ", name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
