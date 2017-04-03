create table Practitioner_authorizations (
	id number primary key,
	hpr_number varchar(40) not null references practitioners(hpr_number),
	authorization_code varchar(20) not null
);

CREATE INDEX authorization_code_key ON PRACTITIONER_AUTHORIZATIONS(hpr_number, authorization_code);
