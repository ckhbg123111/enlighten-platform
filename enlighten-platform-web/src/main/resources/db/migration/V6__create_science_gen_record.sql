CREATE TABLE IF NOT EXISTS science_gen_record (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	user_id BIGINT NOT NULL,
	tenant_id BIGINT NULL,
	code VARCHAR(128) NOT NULL,
	req_body LONGTEXT NOT NULL,
	resp_content LONGTEXT NULL,
	success TINYINT(1) NULL,
	error_message VARCHAR(512) NULL,
	create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_science_gen_record_code ON science_gen_record(code);
CREATE INDEX idx_science_gen_record_user_time ON science_gen_record(user_id, create_time);


