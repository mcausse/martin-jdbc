
drop table xyz if exists;

create table xyz (

	id smallint primary key,
	
	byte1 tinyint,
	byte2 tinyint,
	
	short1 tinyint,
	short2 tinyint,
	
	int1 smallint,
	int2 smallint,
	
	long1 integer,
	long2 integer,
	
	float1 float,
	float2 float,
	
	double1 double,
	double2 double,
	
	boolean1 bit,
	boolean2 bit,
	
	string1 varchar(15),
	
	sex varchar(15),
	
	bytes1 VARBINARY(256),
	
	big_decimal numeric(15,2),
	
	date1 timestamp
);

