CREATE TABLE `request_forward` (
  `id` bigint(16) NOT NULL AUTO_INCREMENT,
  `timeout` VARCHAR(45) NULL,
  `request_name` VARCHAR(45) NULL,
  `content_type` VARCHAR(45) NULL,
  `forward_url` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));