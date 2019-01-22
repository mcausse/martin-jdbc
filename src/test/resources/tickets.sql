/*

	 tickets --* ticket_user *-- users
   	   |                          |
   	   +----* ticket_action *-----+

*/


drop table comments if exists;
drop table options if exists;
drop table users if exists;
drop table votrs if exists;


drop table actions if exists;
drop table ticket_user if exists;
drop table tickets if exists;
drop table users if exists;


create table users (
	 id_user integer primary key generated by default as identity(start with 100),
	 email varchar(100) not null
);


create table tickets (
	 id_tick integer primary key generated by default as identity(start with 10),
	 title varchar(100) not null,
	 created timestamp not null,
	 id_user_created integer
);
alter table tickets add foreign key (id_user_created) references users(id_user);


create table ticket_user (
	id_tick integer,
	id_user integer,
	joined_moment timestamp not null
);
alter table ticket_user add constraint pk_id_ticket_user primary key (id_tick,id_user);
alter table ticket_user add foreign key (id_tick) references tickets(id_tick);
alter table ticket_user add foreign key (id_user) references users(id_user);

create table actions (
	id_action integer primary key not null,
	id_tick integer,
	id_user integer,
	state varchar(20),
	moment timestamp not null,
	message varchar(200) not null
);
alter table actions add foreign key (id_tick) references tickets(id_tick);
alter table actions add foreign key (id_user) references users(id_user);

drop sequence seq_actions if exists;
create sequence seq_actions start with 10;






