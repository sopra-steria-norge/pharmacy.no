create table Last_import_time (
	import_name varchar(40),
	last_import_time datetime not null,
	source varchar not null,
	primary key (import_name, source)
);
