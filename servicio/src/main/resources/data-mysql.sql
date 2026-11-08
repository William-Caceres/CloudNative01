INSERT INTO servicio (nombre, tipo_servicio, num_habitacion, nivel_servicio, descripcion, precio, capacidad, disponible)
SELECT 'Habitación Estándar', 'habitacion', 101, 'invitado', 'Habitación con 2 camas individuales, baño privado y wifi incluido', 50000, 2, b'1'
WHERE NOT EXISTS (SELECT 1 FROM servicio WHERE nombre = 'Habitación Estándar');

INSERT INTO servicio (nombre, tipo_servicio, num_habitacion, nivel_servicio, descripcion, precio, capacidad, disponible)
SELECT 'Suite VIP', 'habitacion', 201, 'VIP', 'Suite con cama king, jacuzzi, minibar y vista panorámica', 120000, 3, b'1'
WHERE NOT EXISTS (SELECT 1 FROM servicio WHERE nombre = 'Suite VIP');

INSERT INTO servicio (nombre, tipo_servicio, num_habitacion, nivel_servicio, descripcion, precio, capacidad, disponible)
SELECT 'Buffet Desayuno', 'comida', 0, 'miembro', 'Desayuno buffet continental con frutas, panadería y café', 15000, 50, b'1'
WHERE NOT EXISTS (SELECT 1 FROM servicio WHERE nombre = 'Buffet Desayuno');

INSERT INTO servicio (nombre, tipo_servicio, num_habitacion, nivel_servicio, descripcion, precio, capacidad, disponible)
SELECT 'Piscina Climatizada', 'recreativo', 0, 'miembro', 'Piscina techada con temperatura regulada y toallas', 10000, 30, b'1'
WHERE NOT EXISTS (SELECT 1 FROM servicio WHERE nombre = 'Piscina Climatizada');
