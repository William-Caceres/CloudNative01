terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.5"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

locals {
  micros = tomap({
    usuario  = { port = 8083 }
    reserva  = { port = 8081 }
    servicio = { port = 8082 }
  })
}

data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }
}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnet" "ec2" {
  availability_zone = "us-east-1a"
  default_for_az    = true
  vpc_id            = data.aws_vpc.default.id
}

resource "random_password" "db_pass" {
  length  = 20
  special = false
}

# ----------------------------------------------------
# Security Groups
# ----------------------------------------------------
resource "aws_security_group" "ec2" {
  name        = "cloudnative-sg-ec2"
  description = "EP1: 8081-8083 publicos para integracion HTTP del API Gateway (la proteccion real es el JWT que valida cada micro). SSH abierto solo por ser lab temporal."
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 8081
    to_port     = 8083
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]

    description = "API Gateway + acceso directo para verificacion"
  }

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]

    description = "SSH para subir jars (vockey)"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "ec2" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = "t2.micro"
  key_name               = "vockey"
  subnet_id              = data.aws_subnet.ec2.id
  vpc_security_group_ids = [aws_security_group.ec2.id]

  user_data = <<-EOT
    #!/bin/bash
    exec > /var/log/cloudnative-bootstrap.log 2>&1
    set -exuo pipefail
    dnf install -y java-21-amazon-corretto-devel
    dnf install -y https://dev.mysql.com/get/mysql80-community-release-el9-4.noarch.rpm
    dnf config-manager --disable mysql-8.4-lts-community
    dnf config-manager --enable mysql80-community
    dnf install -y mysql-community-server

    systemctl enable --now mysqld

    DB_HOST='127.0.0.1'
    DB_USER='admin'
    DB_PASS='${random_password.db_pass.result}'

    for i in $(seq 1 60); do
      mysqladmin ping --silent >/dev/null 2>&1 && break
      sleep 5
    done

    MYSQL_ROOT_PASSWORD=$(grep -oP "(?<=temporary password is generated for root@localhost: )\S+" /var/log/mysqld.log | tail -1)

    mysql -u root -p"$MYSQL_ROOT_PASSWORD" --connect-expired-password -e "
      SET GLOBAL validate_password.policy = LOW;
      ALTER USER 'root'@'localhost' IDENTIFIED BY '$DB_PASS';
      CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASS';
      GRANT ALL PRIVILEGES ON *.* TO '$DB_USER'@'%' WITH GRANT OPTION;
      FLUSH PRIVILEGES;
    "

    for db in h_usuario h_reserva h_servicio; do
      mysql -u root -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS $db CHARACTER SET utf8mb4;"
    done

    mkdir -p /opt/apps

    for entry in "usuario 8083" "reserva 8081" "servicio 8082"; do
      read -r name port <<< "$entry"
      cat > /etc/systemd/system/cn-$name.service <<UNIT
    [Unit]
    Description=CloudNative microservicio $name
    After=network-online.target mysqld.service
    Wants=mysqld.service

    [Service]
    WorkingDirectory=/opt/apps
    Environment=DB_HOST=$DB_HOST
    Environment=DB_PORT=3306
    Environment=DB_NAME=h_$name
    Environment=DB_USERNAME=$DB_USER
    Environment=DB_PASSWORD=$DB_PASS
    Environment=SERVER_PORT=$port
    ExecStart=/usr/bin/java -jar /opt/apps/$${name}-0.0.1-SNAPSHOT.jar
    Restart=always
    RestartSec=5

    [Install]
    WantedBy=multi-user.target
    UNIT
      systemctl daemon-reload
      systemctl enable cn-$name
    done
  EOT

  tags = {
    Project     = "CloudNative01"
    Environment = "ep1"
  }
}

# ----------------------------------------------------
# API Gateway
# ----------------------------------------------------
resource "aws_api_gateway_rest_api" "api" {
  name        = "cloudnative-ep1"
  description = "API Gateway EP1: 3 microservicios Spring Boot en EC2"

  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

resource "aws_api_gateway_resource" "base" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  parent_id   = aws_api_gateway_rest_api.api.root_resource_id
  path_part   = "api"
}

resource "aws_api_gateway_resource" "v1" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  parent_id   = aws_api_gateway_resource.base.id
  path_part   = "v1"
}

resource "aws_api_gateway_resource" "micro_path" {
  for_each    = local.micros
  rest_api_id = aws_api_gateway_rest_api.api.id
  parent_id   = aws_api_gateway_resource.v1.id
  path_part   = each.key
}

resource "aws_api_gateway_resource" "micro" {
  for_each    = local.micros
  rest_api_id = aws_api_gateway_rest_api.api.id
  parent_id   = aws_api_gateway_resource.micro_path[each.key].id
  path_part   = "{proxy+}"
}

resource "aws_api_gateway_method" "any" {
  for_each      = local.micros
  rest_api_id   = aws_api_gateway_rest_api.api.id
  resource_id   = aws_api_gateway_resource.micro[each.key].id
  http_method   = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "any" {
  for_each                = local.micros
  rest_api_id             = aws_api_gateway_rest_api.api.id
  resource_id             = aws_api_gateway_resource.micro[each.key].id
  http_method             = aws_api_gateway_method.any[each.key].http_method
  type                    = "HTTP_PROXY"
  integration_http_method = "ANY"
  uri                     = "http://${aws_instance.ec2.public_dns}:${each.value.port}/api/v1/${each.key}/{proxy}"
}

resource "aws_api_gateway_deployment" "prod" {
  rest_api_id = aws_api_gateway_rest_api.api.id
  stage_name  = "prod"

  depends_on = [
    aws_api_gateway_integration.any,
  ]
}

# ----------------------------------------------------
# Outputs
# ----------------------------------------------------
output "ec2_public_dns" {
  value = aws_instance.ec2.public_dns
}

output "db_password" {
  value     = random_password.db_pass.result
  sensitive = true
}

output "api_gateway_url" {
  value = "https://${aws_api_gateway_rest_api.api.id}.execute-api.us-east-1.amazonaws.com/prod"
}