create table Practitioner_authorizations (
	hpr_number varchar(40) not null references practitioners(hpr_number),
	authorization_code varchar(20) not null
);
