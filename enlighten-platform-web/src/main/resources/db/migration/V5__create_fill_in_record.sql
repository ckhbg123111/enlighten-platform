CREATE TABLE IF NOT EXISTS fill_in_record (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	user_id BIGINT NOT NULL,
	tenant_id BIGINT NULL,
	req_content TEXT NOT NULL,
	resp_content LONGTEXT NULL,
	success TINYINT(1) NULL,
	error_message VARCHAR(512) NULL,
	create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fill_in_record_user_time ON fill_in_record(user_id, create_time);


