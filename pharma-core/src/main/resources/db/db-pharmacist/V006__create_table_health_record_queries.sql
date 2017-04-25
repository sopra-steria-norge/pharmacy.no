create table health_record_queries (
	query_id varchar(40) not null primary key,
	patient_id varchar(40) not null,
	operator_hpr_number varchar(20) not null,
	operator_id_token text not null,
	organization_her_number varchar(10) not null,
	purpose varchar(50) not null,
	documentation text,
	requestor_id_type varchar(50),
	requestor_id_number varchar(50)
);

