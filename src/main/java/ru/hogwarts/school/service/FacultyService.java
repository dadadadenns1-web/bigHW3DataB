
package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.Collection;
import java.util.Collections;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;

    @Autowired
    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Collection<Faculty> getFacultyCollection() {
        return facultyRepository.findAll();
    }

    public Collection<Faculty> getFacultyByNameOrColor(String name, String color) {
        boolean isNameNull = name == null;
        boolean isColorNull = color == null;
        if (isNameNull && isColorNull) {
            return Collections.emptyList();
        }
        if ((!isNameNull && name.isBlank()) || (!isColorNull && color.isBlank())) {
            return null;
        }
        return facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(name, color);
    }

    public Faculty getFacultyById(long id) {
        return facultyRepository.findById(id).orElse(null);
    }

    public Collection<Student> getAllStudentsByFacultyId(long id) {
        if (facultyRepository.existsById(id)) {
            return facultyRepository.getReferenceById(id).getAllStudents();
        } else {
            return null;
        }
    }

    public Faculty save(Faculty faculty) {
        facultyRepository.save(faculty);
        return faculty;
    }

    public Boolean deleteFaculty(long id) {
        Faculty facultyToDelete = facultyRepository.findById(id).orElse(null);
        if (facultyToDelete != null) {
            facultyToDelete.getAllStudents().forEach(s -> s.setFaculty(null));
            facultyRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Faculty updateFaculty(Faculty faculty) {
        if (facultyRepository.existsById(faculty.getId())) {
            return facultyRepository.save(faculty);
        } else {
            return null;
        }
    }

    public void deleteByNameContains(String substring) {
        facultyRepository.deleteByNameContains(substring);
    }

    public boolean existsByNameContains(String substring) {
        return facultyRepository.existsByNameContains(substring);
    }
}
