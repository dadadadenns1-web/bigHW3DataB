package ru.hogwarts.school.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import java.util.Collection;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Override
    @EntityGraph(attributePaths = "faculty")
    Optional<Student> findById(Long id);

    Collection<Student> findByAgeBetween(int min, int max);

    Optional<Student> findByName(String name);

    @Transactional
    void deleteByNameContains(String name);

    boolean existsByNameAndAge(String name, int age);

    @org.springframework.transaction.annotation.Transactional
    @Modifying
    @Query("update Student s set s.faculty = ?1 where s.id = ?2")
    int updateFacultyById(Faculty faculty, long id);

    @Query("SELECT COUNT(*) FROM Student")
    long getStudentListSize();

    @Query("select avg(age) from Student")
    Float getAverageAge();

    @Query("from Student order by(id) desc limit 5")
    Collection<Student> findLastFiveStudents();

}
