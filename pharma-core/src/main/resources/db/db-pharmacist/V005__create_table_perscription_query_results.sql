create table prescription_query_results (
	id bigint auto_increment primary key,
	query_id varchar(36) not null,
	prescription_id varchar(36),
	prescriber_name text not null,
	medication_name text not null,
	date_written date not null
);
