-- Datos de prueba para los servicios de Prestamo (TCP) y Multas (UDP)

-- Estudiante sin deudas
INSERT INTO estudiante (cedula, nombre, apellido)
VALUES ('7654321', 'Ana', 'Benitez')
ON CONFLICT (cedula) DO NOTHING;

-- Prestamo vencido hace 10 dias para el estudiante 1234567 (multa esperada: 20000 Gs)
INSERT INTO prestamo (id_libro, cedula_estudiante, fecha_inicio, fecha_limite_devolucion, estado)
VALUES ((SELECT id FROM libro LIMIT 1), '1234567', CURRENT_DATE - 20, CURRENT_DATE - 10, 'ACTIVO');