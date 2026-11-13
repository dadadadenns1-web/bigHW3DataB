
package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(FacultyService.class);

    @Autowired
    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Collection<Faculty> getFacultyCollection() {
        logger.info("Was invoked method getFacultyCollection()");
        return facultyRepository.findAll();
    }

    public Collection<Faculty> getFacultyByNameOrColor(String name, String color) {
        logger.info("Was invoked method getFacultyByNameOrColor(String name = {}, String color = {})", name, color);
        boolean isNameNull = name == null;
        boolean isColorNull = color == null;
        if (isNameNull && isColorNull) {
            logger.warn("Attempt to find faculties without search parameters");
            return Collections.emptyList();
        }
        if ((!isNameNull && name.isBlank()) || (!isColorNull && color.isBlank())) {
            logger.error("Attempt to find faculties with blank name and/or color search parameters");
            return null;
        }
        return facultyRepository.findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(name, color);
    }

    public Faculty getFacultyById(long id) {
        logger.info("Was invoked method getFacultyById(long id = {})", id);
        Faculty faculty = facultyRepository.findById(id).orElse(null);
        if (faculty == null) {
            logger.error("Attempt to get non-existent faculty, passed faculty id = {}", id);
        }
        return faculty;
    }

    public Collection<Student> getAllStudentsByFacultyId(long id) {
        logger.info("Was invoked method getAllStudentsByFacultyId(long id = {})", id);
        if (facultyRepository.existsById(id)) {
            return facultyRepository.getReferenceById(id).getAllStudents();
        } else {
            logger.error("Attempt to get student list of non-existent faculty, passed faculty id = {}", id);
            return null;
        }
    }

    public Faculty save(Faculty faculty) {
        logger.info("Was invoked method save(Faculty faculty = {})", faculty);
        return facultyRepository.save(faculty);
    }

    public Boolean deleteFaculty(long id) {
        logger.info("Was invoked method deleteFaculty(long id = {})", id);
        Faculty facultyToDelete = facultyRepository.findById(id).orElse(null);
        if (facultyToDelete != null) {
            facultyToDelete.getAllStudents().forEach(s -> s.setFaculty(null));
            facultyRepository.deleteById(id);
            return true;
        } else {
            logger.error("Attempt to delete non-existent faculty, passed faculty id = {}", id);
            return false;
        }
    }

    public Faculty updateFaculty(Faculty faculty) {
        logger.info("Was invoked method updateFaculty(Faculty faculty = {})", faculty);
        if (facultyRepository.existsById(faculty.getId())) {
            return facultyRepository.save(faculty);
        } else {
            logger.error("Attempt to update non-existent faculty, passed faculty id = {}", faculty.getId());
            return null;
        }
    }

    public void deleteByNameContains(String substring) {
        logger.info("Was invoked method deleteByNameContains(String substring = {})", substring);
        facultyRepository.deleteByNameContains(substring);
    }

    public boolean existsByNameContains(String substring) {
        logger.info("Was invoked method existsByNameContains(String substring = {})", substring);
        return facultyRepository.existsByNameContains(substring);
    }
}
