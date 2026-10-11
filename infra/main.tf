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

# Stack EV1+EV2: 7 EC2 t2.micro.
#  - frontend: nginx + build React
#  - usuario:  microservicio + MySQL (BD en la misma maquina = 1 EC2 menos)
#  - reserva / servicio / bff: microservicio (reserva y servicio publican eventos)
#  - cn-rabbit: cluster RabbitMQ de 2 nodos (docker compose)
#  - cn-rabbit-admin: microservicio administrador de RabbitMQ (:8085)
# Para probar rapido
# NOTA: los heredocs user_data van a columna 0 a proposito (cloud-init no tolera espacios antes del shebang).
locals {
  microservicios = {
    usuario        = { port = 8083 }
    reserva        = { port = 8081 }
    servicio       = { port = 8082 }
    bff            = { port = 8084 }
    "rabbit-admin" = { port = 8085 }
  }
  microservicios_app = {
    reserva  = { port = 8081 }
    servicio = { port = 8082 }
    bff      = { port = 8084 }
  }
  microservicios_public_ip = merge(
    { usuario = aws_instance.usuario.public_ip },
    { "rabbit-admin" = aws_instance.rabbit_admin.public_ip },
    { for k, v in aws_instance.microservicio : k => v.public_ip }
  )
}

data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }
}

# Origenes CORS extra (el frontend en EC2 se agrega en cors_configuration)
variable "frontend_origins" {
  description = "Origenes CORS adicionales permitidos (p.ej. el dominio real del frontend en cloud)."
  type        = list(string)
  default     = ["http://localhost:5173", "http://localhost:4173"]
}

# Origenes CORS efectivos (gateway + variable CORS_ORIGINS de los microservicios):
# los extra mas la Elastic IP del frontend, que es estable entre sesiones del lab.
locals {
  cors_origins_list = concat(var.frontend_origins, ["http://${aws_eip.frontend.public_ip}"])
  cors_origins_env  = join(",", local.cors_origins_list)
}

# Usuario del cluster RabbitMQ de EV2 (la password la genera random_password).
variable "rabbit_user" {
  description = "Usuario del cluster RabbitMQ de EV2."
  default     = "hotel"
}

# Tenant y audience de Entra ID (identificadores publicos de la app del curso).
variable "azure_tenant_id" {
  description = "Tenant ID de Entra ID."
  default     = "e34a6311-e69d-4a97-bedf-2ad7cc5455a0"
}

variable "azure_audience" {
  description = "Audiences aceptadas por el authorizer JWT (formato crudo y api://)."
  type        = list(string)
  default     = ["46cda96c-1ab7-42af-a838-5ad46de31226", "api://46cda96c-1ab7-42af-a838-5ad46de31226"]
}

resource "random_password" "db_pass" {
  length  = 20
  special = false
}

# Credencial del cluster RabbitMQ de EV2: se genera sola y se recupera con
# terraform output -raw rabbit_pass
resource "random_password" "rabbit_pass" {
  length  = 24
  special = false
}

# VPC propia: 1 VPC + 1 subred publica + IGW + ruta por defecto
resource "aws_vpc" "this" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = { Name = "cloudnative-vpc" }
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id
  tags   = { Name = "cloudnative-igw" }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.this.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "us-east-1a"
  map_public_ip_on_launch = true

  tags = { Name = "cloudnative-subnet-public" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id
  tags   = { Name = "cloudnative-rt-public" }
}

resource "aws_route" "to_internet" {
  route_table_id         = aws_route_table.public.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.this.id
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# Security Group unico para las 7 EC2. Las reglas van como recursos aparte:
# editarlas no reemplaza el SG ni desasocia las instancias.
resource "aws_security_group" "ec2" {
  name = "cloudnative-sg-ec2"
  # La description no se cambia: editarla en AWS reemplazaria el SG completo.
  description = "EP1: 8081-8084 publicos (API Gateway + verificacion), 80 frontend, 22 SSH lab, 3306 interno entre EC2."
  vpc_id      = aws_vpc.this.id
}

resource "aws_security_group_rule" "ec2_apps" {
  type              = "ingress"
  security_group_id = aws_security_group.ec2.id
  from_port         = 8081
  to_port           = 8085
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  description       = "API Gateway + acceso directo de verificacion"
}

resource "aws_security_group_rule" "ec2_http" {
  type              = "ingress"
  security_group_id = aws_security_group.ec2.id
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  description       = "Frontend nginx"
}

resource "aws_security_group_rule" "ec2_ssh" {
  type              = "ingress"
  security_group_id = aws_security_group.ec2.id
  from_port         = 22
  to_port           = 22
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"] # NOSONAR: SSH del despliegue scp/ssh (la IP del lab cambia por sesion, solo la llave del despliegue e instancias efimeras)
  description       = "SSH para el despliegue (par cloudnative-labs)"
}

resource "aws_security_group_rule" "ec2_mysql_interno" {
  type              = "ingress"
  security_group_id = aws_security_group.ec2.id
  from_port         = 3306
  to_port           = 3306
  protocol          = "tcp"
  self              = true
  description       = "MySQL: solo conexion entre las EC2 del grupo"
}

resource "aws_security_group_rule" "ec2_rabbit_interno" {
  type              = "ingress"
  security_group_id = aws_security_group.ec2.id
  from_port         = 5672
  to_port           = 5672
  protocol          = "tcp"
  self              = true
  description       = "RabbitMQ del cluster: solo conexion entre las EC2 del grupo"
}

resource "aws_security_group_rule" "ec2_egress" {
  type              = "egress"
  security_group_id = aws_security_group.ec2.id
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
}

# EC2 frontend: nginx + build React (se sube dist/ por scp)
resource "aws_instance" "frontend" {
  ami                         = data.aws_ami.al2023.id
  instance_type               = "t2.micro"
  key_name                    = aws_key_pair.ev2.key_name
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  associate_public_ip_address = true # NOSONAR: IP publica requerida por diseno (API Gateway HTTP integra por endpoint publico y el despliegue es scp/ssh desde IP dinamica del lab)

  user_data = <<-EOT
#!/bin/bash
exec > /var/log/frontend-bootstrap.log 2>&1
set -exuo pipefail
dnf install -y nginx
# AL2023 trae un server default en nginx.conf que ignora conf.d: se reemplaza por uno propio.
cat > /etc/nginx/nginx.conf <<'NGINXCONF'
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log;
pid /run/nginx.pid;
events { worker_connections 1024; }
http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    sendfile on;
    keepalive_timeout 65;
    types_hash_max_size 4096;
    include /etc/nginx/conf.d/*.conf;
}
NGINXCONF
mkdir -p /var/www/grandhotel
chown ec2-user:ec2-user /var/www/grandhotel
cat > /etc/nginx/conf.d/grandhotel.conf <<'NGINX'
server {
    listen 80;
    server_name _;
    root /var/www/grandhotel;
    index index.html;

    server_tokens off;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    location / {
        try_files $uri $uri/ /index.html;
    }

    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/javascript application/javascript application/json image/svg+xml;
}
NGINX
systemctl enable --now nginx
echo "Listo: sube el contenido de Frontend/dist a /var/www/grandhotel por scp"
EOT

  tags = { Name = "cn-frontend", Project = "CloudNative01" }
}

# Elastic IP del frontend (EV2): URL publica estable entre sesiones del lab.
# La usan el CORS del API Gateway y el de los 5 microservicios.
resource "aws_eip" "frontend" {
  tags = { Name = "cn-frontend-eip", Project = "CloudNative01" }
}

resource "aws_eip_association" "frontend" {
  instance_id   = aws_instance.frontend.id
  allocation_id = aws_eip.frontend.id
}

# EC2 usuario: microservicio + MySQL (los demas microservicios apuntan a su IP privada)
resource "aws_instance" "usuario" {
  ami                         = data.aws_ami.al2023.id
  instance_type               = "t2.micro"
  key_name                    = aws_key_pair.ev2.key_name
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  associate_public_ip_address = true # NOSONAR: IP publica requerida por diseno (API Gateway HTTP integra por endpoint publico y el despliegue es scp/ssh desde IP dinamica del lab)

  user_data = <<-EOT
#!/bin/bash
exec > /var/log/cn-usuario-bootstrap.log 2>&1
set -exuo pipefail
dnf install -y java-21-amazon-corretto-devel
mkdir -p /opt/apps
chown ec2-user:ec2-user /opt/apps
dnf install -y https://dev.mysql.com/get/mysql80-community-release-el9-4.noarch.rpm
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
MYSQL_TMP_PASS="$DB_PASS!"
mysql -u root -p"$MYSQL_ROOT_PASSWORD" --connect-expired-password -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '$MYSQL_TMP_PASS';"
mysql -u root -p"$MYSQL_TMP_PASS" -e "SET GLOBAL validate_password.policy = LOW;"
mysql -u root -p"$MYSQL_TMP_PASS" -e "ALTER USER 'root'@'localhost' IDENTIFIED BY '$DB_PASS';"
mysql -u root -p"$DB_PASS" -e "
CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASS';
GRANT ALL PRIVILEGES ON *.* TO '$DB_USER'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
"
for db in h_usuario h_reserva h_servicio; do
mysql -u root -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS $db CHARACTER SET utf8mb4;"
done
cat > /etc/systemd/system/cn-usuario.service <<UNIT
[Unit]
Description=CloudNative microservicio usuario
After=network-online.target
[Service]
WorkingDirectory=/opt/apps
Environment=DB_HOST=$DB_HOST
Environment=DB_PORT=3306
Environment=DB_NAME=h_usuario
Environment=DB_USERNAME=$DB_USER
Environment=DB_PASSWORD=$DB_PASS
Environment=SERVER_PORT=8083
Environment=CORS_ORIGINS=${local.cors_origins_env}
ExecStart=/usr/bin/java -jar /opt/apps/usuario-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5
[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable cn-usuario
echo "Listo: sube /opt/apps/usuario-0.0.1-SNAPSHOT.jar por scp y ejecuta: systemctl start cn-usuario"
EOT

  tags = { Name = "cn-usuario", Project = "CloudNative01" }
}

# EC2 reserva/servicio: solo microservicio, BD en la instancia usuario
resource "aws_instance" "microservicio" {
  for_each                    = local.microservicios_app
  ami                         = data.aws_ami.al2023.id
  instance_type               = "t2.micro"
  key_name                    = aws_key_pair.ev2.key_name
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  associate_public_ip_address = true # NOSONAR: IP publica requerida por diseno (API Gateway HTTP integra por endpoint publico y el despliegue es scp/ssh desde IP dinamica del lab)

  user_data = <<-EOT
#!/bin/bash
exec > /var/log/cn-${each.key}-bootstrap.log 2>&1
set -exuo pipefail
dnf install -y java-21-amazon-corretto-devel
mkdir -p /opt/apps
chown ec2-user:ec2-user /opt/apps
DB_HOST='${aws_instance.usuario.private_ip}'
DB_USER='admin'
DB_PASS='${random_password.db_pass.result}'
RABBIT_HOST='${aws_instance.cn_rabbit.private_ip}'
RABBIT_USER='${var.rabbit_user}'
RABBIT_PASS='${random_password.rabbit_pass.result}'
cat > /etc/systemd/system/cn-${each.key}.service <<UNIT
[Unit]
Description=CloudNative microservicio ${each.key}
After=network-online.target
[Service]
WorkingDirectory=/opt/apps
Environment=DB_HOST=$DB_HOST
Environment=DB_PORT=3306
Environment=DB_NAME=h_${each.key}
Environment=DB_USERNAME=$DB_USER
Environment=DB_PASSWORD=$DB_PASS
Environment=RABBITMQ_HOST=$RABBIT_HOST
Environment=RABBITMQ_PORT=5672
Environment=RABBITMQ_USER=$RABBIT_USER
Environment=RABBITMQ_PASSWORD=$RABBIT_PASS
Environment=MICROSERVICIOS_USUARIO_URL=${aws_apigatewayv2_api.http.api_endpoint}
Environment=MICROSERVICIOS_RESERVA_URL=${aws_apigatewayv2_api.http.api_endpoint}
Environment=MICROSERVICIOS_SERVICIO_URL=${aws_apigatewayv2_api.http.api_endpoint}
Environment=SERVER_PORT=${each.value.port}
Environment=CORS_ORIGINS=${local.cors_origins_env}
ExecStart=/usr/bin/java -jar /opt/apps/${each.key}-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5
[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable cn-${each.key}
echo "Listo: sube /opt/apps/${each.key}-0.0.1-SNAPSHOT.jar por scp y ejecuta: systemctl start cn-${each.key}"
EOT

  tags = { Name = "cn-${each.key}", Project = "CloudNative01" }
}

# Par de llaves de las 7 EC2, creado desde la clave publica versionada en el repo.
# Si el keypair ya existe: terraform import aws_key_pair.ev2 cloudnative-labs
resource "aws_key_pair" "ev2" {
  key_name   = "cloudnative-labs"
  public_key = file("${path.module}/keys/cloudnative-labs.pub")
}

# EC2 cn-rabbit: cluster RabbitMQ de 2 nodos con docker compose (EV2).
# El compose se sube en el despliegue (infra/rabbit-cluster por scp).
resource "aws_instance" "cn_rabbit" {
  ami                         = data.aws_ami.al2023.id
  instance_type               = "t2.micro"
  key_name                    = aws_key_pair.ev2.key_name
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  associate_public_ip_address = true # NOSONAR: IP publica requerida por diseno (despliegue scp/ssh desde IP dinamica del lab. El broker solo acepta 5672 desde las EC2 del grupo)

  user_data = <<-EOT
#!/bin/bash
exec > /var/log/cn-rabbit-bootstrap.log 2>&1
set -exuo pipefail
dnf install -y docker
systemctl enable --now docker
usermod -aG docker ec2-user
mkdir -p /usr/local/lib/docker/cli-plugins
curl -sSL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
echo "Listo: sube infra/rabbit-cluster por scp, crea .env y ejecuta docker compose up -d + ./unirse-cluster.sh"
EOT

  tags = { Name = "cn-rabbit", Project = "CloudNative01" }
}

# EC2 cn-rabbit-admin: microservicio administrador de RabbitMQ (:8085, EV2).
resource "aws_instance" "rabbit_admin" {
  ami                         = data.aws_ami.al2023.id
  instance_type               = "t2.micro"
  key_name                    = aws_key_pair.ev2.key_name
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  associate_public_ip_address = true # NOSONAR: IP publica requerida por diseno (API Gateway HTTP integra por endpoint publico y el despliegue es scp/ssh desde IP dinamica del lab)

  user_data = <<-EOT
#!/bin/bash
exec > /var/log/cn-rabbit-admin-bootstrap.log 2>&1
set -exuo pipefail
dnf install -y java-21-amazon-corretto-devel
mkdir -p /opt/apps
chown ec2-user:ec2-user /opt/apps
cat > /etc/systemd/system/cn-rabbit-admin.service <<UNIT
[Unit]
Description=CloudNative microservicio rabbit-admin
After=network-online.target
[Service]
WorkingDirectory=/opt/apps
Environment=RABBITMQ_HOST=${aws_instance.cn_rabbit.private_ip}
Environment=RABBITMQ_PORT=5672
Environment=RABBITMQ_USER=${var.rabbit_user}
Environment=RABBITMQ_PASSWORD=${random_password.rabbit_pass.result}
Environment=SERVER_PORT=8085
Environment=CORS_ORIGINS=${local.cors_origins_env}
ExecStart=/usr/bin/java -jar /opt/apps/rabbit-admin-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5
[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable cn-rabbit-admin
echo "Listo: sube /opt/apps/rabbit-admin-0.0.1-SNAPSHOT.jar por scp y ejecuta: systemctl start cn-rabbit-admin"
EOT

  tags = { Name = "cn-rabbit-admin", Project = "CloudNative01" }
}

# API Gateway HTTP API v2 con JWT authorizer (Entra)
resource "aws_apigatewayv2_api" "http" {
  name          = "cloudnative-ep1"
  protocol_type = "HTTP"
  cors_configuration {
    # EP1: antes abierto a cualquier origen, ahora solo frontend propio + local.
    # La IP del frontend es su Elastic IP, estable entre sesiones del lab.
    allow_origins = local.cors_origins_list
    allow_methods = ["GET", "POST", "PUT", "DELETE", "OPTIONS"]
    allow_headers = ["authorization", "content-type"]
    max_age       = 300
  }
}

resource "aws_apigatewayv2_authorizer" "jwt" {
  api_id                           = aws_apigatewayv2_api.http.id
  authorizer_type                  = "JWT"
  name                             = "entra-jwt"
  identity_sources                 = ["$request.header.Authorization"]
  authorizer_result_ttl_in_seconds = 0

  jwt_configuration {
    audience = var.azure_audience
    issuer   = "https://login.microsoftonline.com/${var.azure_tenant_id}/v2.0"
  }
}

resource "aws_apigatewayv2_integration" "http" {
  for_each = local.microservicios

  api_id             = aws_apigatewayv2_api.http.id
  integration_type   = "HTTP_PROXY"
  integration_method = "ANY"
  integration_uri    = "http://${local.microservicios_public_ip[each.key]}:${each.value.port}/api/v1/${each.key}/{proxy}"
}

resource "aws_apigatewayv2_route" "http" {
  for_each = local.microservicios

  api_id             = aws_apigatewayv2_api.http.id
  route_key          = "ANY /api/v1/${each.key}/{proxy+}"
  authorization_type = "JWT"
  authorizer_id      = aws_apigatewayv2_authorizer.jwt.id
  target             = "integrations/${aws_apigatewayv2_integration.http[each.key].id}"
}

# Preflight sin auth: las rutas ANY caen en el authorizer JWT y devuelven 401
# (el navegador exige 2xx en el preflight). Esta ruta NONE lo evita.
resource "aws_apigatewayv2_route" "options" {
  for_each = local.microservicios

  api_id             = aws_apigatewayv2_api.http.id
  route_key          = "OPTIONS /api/v1/${each.key}/{proxy+}"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.http[each.key].id}"
}

# Frontend servido por el mismo API Gateway (EV2): https con certificado de AWS,
# mismo origen que la API. Las rutas GET NONE solo sirven el frontend publico
# (las de /api/v1/* conservan su JWT).
resource "aws_apigatewayv2_integration" "frontend_root" {
  api_id             = aws_apigatewayv2_api.http.id
  integration_type   = "HTTP_PROXY"
  integration_method = "GET"
  integration_uri    = "http://${aws_eip.frontend.public_ip}/"
}

resource "aws_apigatewayv2_integration" "frontend" {
  api_id             = aws_apigatewayv2_api.http.id
  integration_type   = "HTTP_PROXY"
  integration_method = "GET"
  integration_uri    = "http://${aws_eip.frontend.public_ip}/{proxy}"
}

resource "aws_apigatewayv2_route" "frontend_root" {
  api_id             = aws_apigatewayv2_api.http.id
  route_key          = "GET /"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.frontend_root.id}"
}

resource "aws_apigatewayv2_route" "frontend" {
  api_id             = aws_apigatewayv2_api.http.id
  route_key          = "GET /{proxy+}"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.frontend.id}"
}

# Logs de acceso del API Gateway (retencion 30 dias, el destroy elimina el grupo)
resource "aws_cloudwatch_log_group" "api" {
  name              = "/aws/apigateway/cloudnative-ep1"
  retention_in_days = 30
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.http.id
  name        = "$default"
  auto_deploy = true

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api.arn
    format = jsonencode({
      requestId      = "$context.requestId"
      ip             = "$context.identity.sourceIp"
      requestTime    = "$context.requestTime"
      httpMethod     = "$context.httpMethod"
      routeKey       = "$context.routeKey"
      status         = "$context.status"
      responseLength = "$context.responseLength"
    })
  }
}

# Outputs
output "frontend_public_ip" {
  value = aws_eip.frontend.public_ip
}

output "usuario_public_ip" {
  value = aws_instance.usuario.public_ip
}

output "usuario_private_ip" {
  value = aws_instance.usuario.private_ip
}

output "microservicios_public_ip" {
  value = { for k, v in aws_instance.microservicio : k => v.public_ip }
}

output "rabbit_private_ip" {
  value = aws_instance.cn_rabbit.private_ip
}

output "rabbit_public_ip" {
  value = aws_instance.cn_rabbit.public_ip
}

output "rabbit_admin_public_ip" {
  value = aws_instance.rabbit_admin.public_ip
}

output "api_url" {
  value = aws_apigatewayv2_api.http.api_endpoint
}

output "db_password" {
  value     = random_password.db_pass.result
  sensitive = true
}

output "rabbit_pass" {
  value     = random_password.rabbit_pass.result
  sensitive = true
}
