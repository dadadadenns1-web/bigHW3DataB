
DELETE FROM public.avatar;
DELETE FROM public.student;
DELETE FROM public.faculty;
INSERT INTO public.faculty (id, name, color) values (1,'Факультет физики','Красный');
INSERT INTO public.student (id, name, age, faculty_id) VALUES (1, 'Анна', 21, 1);
INSERT INTO public.faculty (id, name, color) values (2,'Факультет химии','Жёлтый');
INSERT INTO public.student (id, name, age, faculty_id) VALUES (2, 'Серёга', 27, 2);
INSERT INTO public.student (id, name, age, faculty_id) VALUES (3, 'Олег', 17, 2);
INSERT INTO public.faculty (id, name, color) values (3,'Факультет географии','Зелёный');
INSERT INTO public.student (id, name, age, faculty_id) VALUES (4, 'Иван', 25, 3);
INSERT INTO public.faculty (id, name, color) values (4,'Факультет математики','Синий');
INSERT INTO public.student (id, name, age, faculty_id) VALUES (5, 'Тимофей', 20, 4);
INSERT INTO public.student (id, name, age, faculty_id) VALUES (6, 'Павел', 18, 4);
INSERT INTO public.student (id, name, age, faculty_id) VALUES (7, 'Дима', 15, 4);