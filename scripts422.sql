create table car (
id serial primary key,
model varchar(50),
brand varchar(50),
price int
)
create table driver (
id serial primary key,
name varchar(50),
age int,
license boolean,
car_id int references car (id)
)