-- Возраст студента не может быть меньше 16 лет.
-- Имена студентов должны быть уникальными и не равны нулю.
-- Пара “значение названия” - “цвет факультета” должна быть уникальной.
-- При создании студента без возраста ему автоматически должно присваиваться 20 лет.

alter table student add constraint age_value_constraint check (age >= 16);
alter table student add constraint name_unique_constraint unique(name);
alter table student alter column name set not null;
alter table faculty add constraint name_color_unique_constraint unique(name,color);
alter table student alter column age set default (20);

--Команды для сброса
alter table student drop constraint name_unique_constraint;
alter table student drop constraint age_value_constraint;
alter table student alter column name drop not null;
alter table faculty drop constraint name_color_unique_constraint;


--Команды для просмотра
select * from information_schema.table_constraints where table_name = 'student';
select * from information_schema.table_constraints where table_name = 'faculty';
select * from public.student;
select * from public.faculty;

--Проверка на default значение
INSERT INTO public.student (id,name) VALUES (123125,'Антон_ac45ca5f-9523-45e7-827a-292550498ccd');
delete from public.student where name = 'Антон_ac45ca5f-9523-45e7-827a-292550498ccd';

--проверка на unique значения
INSERT INTO public.faculty (id,name,color) values (124125, 'Математика_8cc74cb3-3937-43aa-b610-4624da4f2ba3','Красный_8cc74cb3-3937-43aa-b610-4624da4f2ba3')
INSERT INTO public.faculty (id,name,color) values (124364, 'Математика_8cc74cb3-3937-43aa-b610-4624da4f2ba3','Красный_8cc74cb3-3937-43aa-b610-4624da4f2ba3')
delete from public.faculty where name like '%8cc74cb3-3937-43aa-b610-4624da4f2ba3%';

--В этом задании по описанию необходимо спроектировать таблицы, связи между ними и корректно определить типы данных.
-- Здесь не важно, какой тип вы выберете, например, для данных, представленных в виде строки (varchar или text).
--  Важно, что вы выберете один из строковых типов, а не числовых, например.

--Описание структуры: у каждого человека есть машина. Причем несколько человек могут пользоваться одной машиной
-- У каждого человека есть имя, возраст и признак того, что у него есть права (или их нет).
--  У каждой машины есть марка, модель и стоимость.
--   Также не забудьте добавить таблицам первичные ключи и связать их.
create table public.car(
id serial primary key,
model varchar(50),
brand varchar(50),
price int
)
create table public.driver(
id serial primary key,
name varchar(50),
age int,
license boolean,
car_id int references car (id)
)
--удаление
drop table driver;
drop table car;
--просмотр
select * from public.driver;
select * from public.car;

--Составить первый JOIN-запрос, чтобы получить информацию обо всех студентах
--(достаточно получить только имя и возраст студента) школы Хогвартс вместе с названиями факультетов.
--Составить второй JOIN-запрос, чтобы получить только тех студентов, у которых есть аватарки.
--В корне проекта создать файл scripts423.sql и поместить в него запрос.

select student.name, student.age, faculty.name from public.student inner join faculty on student.faculty_id = faculty.id;
select student.name from public.student inner join avatar on student.id = avatar.student_id

--просмотр
select * from public.avatar;


