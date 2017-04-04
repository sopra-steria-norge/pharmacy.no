create table Organizations (
	her_number varchar(40) primary key,
	display text not null,
	municipality_code varchar(6),
	business_type varchar(4) not null,
	updated_at datetime not null
);
