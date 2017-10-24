create table dispense_orders (
	id varchar(36) not null primary key,
	patient_id varchar(40) not null,
	patient_name text not null,
	dispensed boolean,
	date_dispensed date,
	customer_signature text,
	dispensing_organization varchar(40)
);
