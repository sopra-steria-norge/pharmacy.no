create table medication_orders (
	id bigint auto_increment primary key,
	dispense_order_id varchar(36),
	prescriber_id varchar(36) not null,
	prescriber_name text not null,
	prescription_id varchar(36),
	date_written date,
	dosage_text text,
	medication_id varchar(30)
);
