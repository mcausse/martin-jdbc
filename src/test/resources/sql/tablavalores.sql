

drop table tabla_valores if exists;

create table tabla_valores (
	name varchar(30),
	code varchar(30), 
	value varchar(100) not null,
);
ALTER TABLE tabla_valores ADD PRIMARY KEY(name,code);

