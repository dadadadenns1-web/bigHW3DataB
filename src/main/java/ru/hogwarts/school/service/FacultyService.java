
package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
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

    public Collection<Faculty> getFacultyCollection(String color) {
        if (color != null && color.isBlank()) {
            return Collections.emptyList();
        }
        return facultyRepository.findAll().stream().filter(f ->
                (color == null || f.getColor().equals(color))
        ).toList();
    }

    public Faculty getFacultyById(long id) {
        return facultyRepository.findById(id).orElse(null);
    }

    public Faculty addNewFaculty(Faculty faculty) {
        facultyRepository.save(faculty);
        return faculty;
    }

    public Boolean deleteFaculty(long id) {
        if(facultyRepository.existsById(id)){
            facultyRepository.deleteById(id);
            return true;
    }else{
            return false;
        }
    }

    public Faculty updateFaculty(Faculty faculty) {
        if(facultyRepository.existsById(faculty.getId())){
            return facultyRepository.save(faculty);
        }else{
            return null;
        }
    }

}
