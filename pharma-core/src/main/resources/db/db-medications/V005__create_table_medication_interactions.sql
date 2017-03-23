create table medication_interactions (
	id varchar(40) primary key,
	severity varchar(40) not null,
	clinical_consequence text,
	interaction_mechanism text
);

create table interacting_substance (
	interaction_id varchar(40) not null references medication_interactions(id),
	atc_code varchar(10) not null
)
	
