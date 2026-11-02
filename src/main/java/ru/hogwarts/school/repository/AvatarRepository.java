package ru.hogwarts.school.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.AvatarWithIgnoredData;
import ru.hogwarts.school.model.Student;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    Avatar findByStudent(Student student);

    @Transactional
    @Modifying
    @Query("update Avatar a set a.fileSize = ?1, a.mediaType = ?2, a.data = ?3 where a.id = ?4")
    void updateFileSizeAndMediaTypeAndDataById(long fileSize, String mediaType, byte[] data, long id);


    Avatar findByStudent_Id(long id);


    Page<AvatarWithIgnoredData> findAllBy(Pageable pageable);


}
