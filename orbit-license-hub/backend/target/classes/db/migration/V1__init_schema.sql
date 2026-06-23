CREATE TABLE tenants (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL UNIQUE,
  purchased_licenses INT NOT NULL,
  assigned_licenses INT NOT NULL,
  status VARCHAR(20) NOT NULL
);

CREATE TABLE client_applications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  name VARCHAR(140) NOT NULL,
  logo_url VARCHAR(500),
  base_color VARCHAR(20) NOT NULL,
  environment VARCHAR(10) NOT NULL,
  p12_file_ref VARCHAR(500),
  p12_password_encrypted TEXT NOT NULL,
  retention_minutes INT NOT NULL,
  public_key_pem LONGTEXT NOT NULL,
  allocated_licenses INT NOT NULL,
  client_api_key VARCHAR(140) NOT NULL UNIQUE,
  client_secret_encrypted TEXT NOT NULL,
  status VARCHAR(20) NOT NULL,
  CONSTRAINT fk_application_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE TABLE licenses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  application_id BIGINT NULL,
  device_code VARCHAR(200) NOT NULL,
  status VARCHAR(20) NOT NULL,
  last_used_at TIMESTAMP NULL,
  CONSTRAINT fk_license_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  CONSTRAINT fk_license_application FOREIGN KEY (application_id) REFERENCES client_applications(id)
);

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(120) NOT NULL UNIQUE,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(30) NOT NULL,
  tenant_id BIGINT NULL,
  enabled BOOLEAN NOT NULL,
  CONSTRAINT fk_user_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE TABLE user_extra_permissions (
  user_id BIGINT NOT NULL,
  permission VARCHAR(120) NOT NULL,
  CONSTRAINT fk_permission_user FOREIGN KEY (user_id) REFERENCES users(id)
);
