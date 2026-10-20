package ru.hogwarts.school.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.hogwarts.school.model.Faculty;

import java.util.Collection;
import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Collection<Faculty> findByNameContainingIgnoreCaseOrColorContainingIgnoreCase(String name, String color);

    @Transactional
    void deleteByNameContains(String substring);

    boolean existsByNameContains(String name);


    @Override
    @EntityGraph(attributePaths = "studentList")
    Optional<Faculty> findById(Long id);
}
