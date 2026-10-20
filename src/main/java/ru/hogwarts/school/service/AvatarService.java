package ru.hogwarts.school.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.exception.FileSizeException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
public class AvatarService {

    @Value("${path.to.avatars.folder}")
    private String avatarsDir;

    private final StudentRepository studentRepository;
    private final AvatarRepository avatarRepository;

    @Autowired
    public AvatarService(StudentRepository studentRepository, AvatarRepository avatarRepository) {
        this.studentRepository = studentRepository;
        this.avatarRepository = avatarRepository;
    }

    public String getAvatarsDir() {
        return avatarsDir;
    }

    public Avatar findByStudent_Id(long id) {
        return avatarRepository.findByStudent_Id(id);
    }

    public void uploadAvatar(Long studentId, MultipartFile avatarMpFile) throws EntityNotFoundException, IOException, FileSizeException {
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new EntityNotFoundException("Студент не найден по id - " + studentId));

        long fileSize = avatarMpFile.getSize();
        var maxFileSize = 1024 * 300;
        if (fileSize > maxFileSize) {
            throw new FileSizeException("Максимальный размер файла " + (maxFileSize / 1024) + " KiB");
        }

        Path filePath = Path.of(avatarsDir, studentId + "."
                + Optional.ofNullable(StringUtils.getFilenameExtension(avatarMpFile.getOriginalFilename())).orElse(" "));
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);
        //Сохранение на диске
        try (
                InputStream is = avatarMpFile.getInputStream();
                OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
                BufferedInputStream bis = new BufferedInputStream(is, 1024);
                BufferedOutputStream bos = new BufferedOutputStream(os, 1024)
        ) {
            bis.transferTo(bos);
        }

        //Сохранение в БД
        Avatar avatar = avatarRepository.findByStudent(student);
        if (avatar == null) {
            avatar = new Avatar(filePath.toString(), fileSize, avatarMpFile.getContentType(), avatarMpFile.getBytes(), student);
            avatarRepository.save(avatar);
        } else {
            avatarRepository.updateFileSizeAndMediaTypeAndDataById(fileSize, avatarMpFile.getContentType(), avatarMpFile.getBytes(), avatar.getId());
        }

    }
}
