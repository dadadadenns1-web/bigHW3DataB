--liquibase formatted sql

--changeset adenis:1
CREATE INDEX student_name_index ON student (name);

--changeset adenis:2
CREATE INDEX faculty_name_color_index ON faculty (name,color);