create table Medications (
	id bigint auto_increment primary key,
	display text,
	product_id varchar(15),
	exchange_group_id varchar(50),
	trinn_price number,
	retail_price number
);
