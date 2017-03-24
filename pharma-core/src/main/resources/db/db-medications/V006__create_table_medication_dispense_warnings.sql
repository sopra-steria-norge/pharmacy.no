create table medication_dispense_actions (
	dispense_id bigint not null references medication_dispenses(id),
	interacting_dispense_id bigint references medication_dispenses(id),
	interaction_id varchar(40),
	interacting_dispense_display text,
	warning_action varchar(40),
	warning_remark text
);
