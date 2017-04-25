create table Patients (
	id varchar(40) primary key,
	first_name text not null,
	last_name text not null,
	encrypted_national_id text
);
