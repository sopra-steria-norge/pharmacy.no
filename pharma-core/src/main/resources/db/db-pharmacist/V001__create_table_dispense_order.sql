create table dispense_orders (
	id varchar(36) not null primary key,
	dispensed boolean,
	customer_signature text
);
