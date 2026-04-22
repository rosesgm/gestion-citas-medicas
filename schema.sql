CREATE DATABASE IF NOT EXISTS gestion_citas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestion_citas;

CREATE TABLE IF NOT EXISTS especialidades (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS medicos (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    especialidad_id INT NOT NULL,
    consultorio     VARCHAR(50),
    disponible      TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_medico_especialidad FOREIGN KEY (especialidad_id) REFERENCES especialidades(id)
);

CREATE TABLE IF NOT EXISTS pacientes (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    nombre   VARCHAR(150) NOT NULL,
    correo   VARCHAR(150),
    telefono VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS citas (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT NOT NULL,
    medico_id   INT NOT NULL,
    fecha       DATE NOT NULL,
    hora        TIME NOT NULL,
    estado      ENUM('PROGRAMADA','CONFIRMADA','EN_CURSO','FINALIZADA','CANCELADA') NOT NULL DEFAULT 'PROGRAMADA',
    motivo      VARCHAR(255),
    CONSTRAINT fk_cita_paciente     FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    CONSTRAINT fk_cita_medico       FOREIGN KEY (medico_id)   REFERENCES medicos(id),
    CONSTRAINT uq_medico_fecha_hora UNIQUE (medico_id, fecha, hora)
);

INSERT INTO especialidades (nombre, descripcion) VALUES
    ('Cardiología',  'Enfermedades del corazón y sistema circulatorio'),
    ('Pediatría',    'Atención médica a niños y adolescentes'),
    ('Dermatología', 'Enfermedades de la piel');

INSERT INTO medicos (nombre, especialidad_id, consultorio, disponible) VALUES
    ('Dr. Carlos Ramírez', 1, 'Consultorio 101', 1),
    ('Dra. Laura Pérez',   2, 'Consultorio 205', 1),
    ('Dr. Miguel Torres',  3, 'Consultorio 312', 1);

INSERT INTO pacientes (nombre, correo, telefono) VALUES
    ('Ana García',    'ana.garcia@correo.com',    '6221001001'),
    ('Luis Martínez', 'luis.martinez@correo.com', '6221002002');