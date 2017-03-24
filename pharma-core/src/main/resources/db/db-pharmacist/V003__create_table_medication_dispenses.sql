create table medication_dispenses (
	id bigint auto_increment primary key,
	dispense_order_id varchar(36),
	authorizing_prescription_id varchar(36),
	dispenser_id varchar(36),
	date_dispensed date,
	medication_id varchar(30),
	price decimal
);
