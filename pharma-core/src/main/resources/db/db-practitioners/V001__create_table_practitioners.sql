create table Practitioners (
	hpr_number varchar(40) primary key,
	first_name text not null,
	last_name text not null,
	encrypted_national_id text,
	date_of_birth date not null,
	updated_at datetime not null
);

CREATE INDEX practitioner_pk ON PRACTITIONERS(hpr_number);
