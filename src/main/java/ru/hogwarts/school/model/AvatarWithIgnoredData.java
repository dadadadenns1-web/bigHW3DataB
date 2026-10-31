package ru.hogwarts.school.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public interface AvatarWithIgnoredData {

    long getId();
    String getMediaType();
    String getFilePath();
    long getFileSize();
    @JsonIgnore
    byte[] getData();
    long getStudentId();

    default String getAvatarUrl(){
        return ServletUriComponentsBuilder.fromCurrentContextPath().toUriString() + "/avatar/" + getStudentId();
    }
}
