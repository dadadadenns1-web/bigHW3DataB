package ru.hogwarts.school.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.exception.FileSizeException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.AvatarWithIgnoredData;
import ru.hogwarts.school.service.AvatarService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

@RestController
@RequestMapping("avatar")
public class AvatarController {

    private final AvatarService avatarService;

    @Autowired
    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @PostMapping(path = "/{student_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<String> uploadAvatar(@PathVariable Long student_id, @RequestParam MultipartFile avatar) throws IOException {
        try {
            avatarService.uploadAvatar(student_id, avatar);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (FileSizeException e) {
            return ResponseEntity.status(413).body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getAvatarsInPage")
    @Transactional
    public Collection<AvatarWithIgnoredData> getAllAvatarsInPage(@RequestParam int page, @RequestParam int size) {
        return avatarService.getAvatarPage(page, size);
    }

    @GetMapping(path = "/{student_id}")
    @Transactional
    public ResponseEntity<byte[]> getAvatarData(@PathVariable long student_id) {
        Avatar avatar = avatarService.findByStudent_Id(student_id);
        if (avatar == null) {
            return ResponseEntity.status(404).build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(avatar.getMediaType()));
        headers.setContentLength(avatar.getFileSize());
        return ResponseEntity.status(200).headers(headers).body(avatar.getData());
    }

    @Transactional
    @GetMapping()
    public void downloadAvatar(@RequestParam Long student_id, HttpServletResponse response) throws IOException {
        Avatar avatar = avatarService.findByStudent_Id(student_id);
        if (avatar == null) {
            response.setStatus(404);
            return;

        }
        Path path = Path.of(avatar.getFilePath());

        try (InputStream is = Files.newInputStream(path);
             OutputStream os = response.getOutputStream()) {
            response.setStatus(200);
            response.setContentType(avatar.getMediaType());
            response.setContentLength((int) avatar.getFileSize());
            is.transferTo(os);
        }
    }

}
