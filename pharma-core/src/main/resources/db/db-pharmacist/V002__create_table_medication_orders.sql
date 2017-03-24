create table medication_orders (
	id bigint auto_increment primary key,
	dispense_order_id varchar(36),
	prescriber_id varchar(36),
	date_written date,
	medication_id varchar(30)
);
