aws_region         = "ap-northeast-2"
environment        = "prod"
project_name       = "api"
vpc_cidr           = "10.1.0.0/16"
public_subnet_cidrs = [
  "10.1.1.0/24",
  "10.1.2.0/24",
]
private_subnet_cidrs = [
  "10.1.11.0/24",
  "10.1.12.0/24",
]
availability_zones = [
  "ap-northeast-2a",
  "ap-northeast-2c",
]
instance_type      = "t3.micro"
key_name           = "replace-with-your-keypair-name"
ami_id             = "replace-with-your-ami-id"
app_port           = 8080
allowed_ssh_cidr   = "0.0.0.0/0"
db_name            = "appdb"
db_username        = "admin"
db_instance_class  = "db.t3.micro"
db_allocated_storage = 20
db_engine_version  = "8.0"
