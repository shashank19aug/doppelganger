CREATE TABLE `simulator_response` (
  `id` bigint(16) NOT NULL AUTO_INCREMENT,
  `simulator_config_id` bigint(16) NULL,
  `validation` VARCHAR(45) NULL,
  `response_code` INT NULL DEFAULT NULL,
  `response` LONGTEXT NULL DEFAULT NULL,
  `timeout_period` INT NULL DEFAULT NULL,
  PRIMARY KEY (`id`));