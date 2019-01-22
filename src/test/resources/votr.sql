
drop sequence seq_votrs if exists;
create sequence seq_votrs start with 100;

drop sequence seq_users if exists;
create sequence seq_users start with 200;

drop sequence seq_options if exists;
create sequence seq_options start with 300;

drop sequence seq_comments if exists;
create sequence seq_comments start with 400;


drop table actions if exists;
drop table ticket_user if exists;
drop table tickets if exists;
drop table users if exists;

drop table comments if exists;
drop table options if exists;
drop table users if exists;
drop table votrs if exists;

create table votrs (
	votr_id smallint primary key,
	votr_hash varchar(15) not null, 
	title varchar(100) not null,
	descr varchar(500) not null,
	creat_date timestamp not null 
);

create table users (
	user_id integer primary key,
	user_hash varchar(15) not null,
	email varchar(100) not null,
	alias varchar(100),
	
	votr_id smallint not null,
	
	option_id integer,
	option_date timestamp
);

create table options (
	option_id integer primary key,
	
	title varchar(100) not null,
	descr varchar(500) not null,
	
	votr_id smallint not null
);

create table comments (
	comment_id integer primary key,
	
	comment_date timestamp not null,
	comment varchar(1024) not null,

	votr_id smallint not null,
	user_id integer not null
);


alter table users add foreign key (votr_id) references votrs(votr_id);
alter table users add foreign key (option_id) references options(option_id);
alter table options add foreign key (votr_id) references votrs(votr_id);
alter table comments add foreign key (votr_id) references votrs(votr_id);
alter table comments add foreign key (user_id) references users(user_id);





