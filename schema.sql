DROP DATABASE gestion_citas;
CREATE DATABASE IF NOT EXISTS gestion_citas CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestion_citas;

-- ------------------------------------------------------------
-- Tablas base del sistema
-- ------------------------------------------------------------

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

-- ------------------------------------------------------------
-- Tablas de autenticación Google (admin/recepcionista)
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS usuarios_google (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    google_sub VARCHAR(255) NOT NULL,
    nombre     VARCHAR(150) NOT NULL,
    correo     VARCHAR(150) NOT NULL,
    foto_url   VARCHAR(500),
    creado_en  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_google_sub UNIQUE (google_sub)
);

CREATE TABLE IF NOT EXISTS google_tokens (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    usuario_google_id INT NOT NULL,
    access_token      TEXT NOT NULL,
    refresh_token     TEXT,
    token_type        VARCHAR(20) DEFAULT 'Bearer',
    expires_at        DATETIME,
    scope             TEXT,
    actualizado_en    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_usuario FOREIGN KEY (usuario_google_id) REFERENCES usuarios_google(id),
    CONSTRAINT uq_token_usuario UNIQUE (usuario_google_id)
);

-- Registra el estado de sincronización de cada cita con Google Calendar
CREATE TABLE IF NOT EXISTS citas_sync_google (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    cita_id           INT NOT NULL,
    usuario_google_id INT NOT NULL,
    google_event_id   VARCHAR(255),
    estado_sync       ENUM('PENDIENTE','SINCRONIZADA','ERROR') NOT NULL DEFAULT 'PENDIENTE',
    ultimo_error      VARCHAR(500),
    actualizado_en    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sync_cita    FOREIGN KEY (cita_id)           REFERENCES citas(id),
    CONSTRAINT fk_sync_usuario FOREIGN KEY (usuario_google_id) REFERENCES usuarios_google(id),
    CONSTRAINT uq_sync_cita    UNIQUE (cita_id)
);

-- ------------------------------------------------------------
-- Datos de prueba
-- ------------------------------------------------------------

INSERT INTO especialidades (nombre, descripcion) VALUES
    ('Cardiología',  'Enfermedades del corazón y sistema circulatorio'),
    ('Pediatría',    'Atención médica a niños y adolescentes'),
    ('Dermatología', 'Enfermedades de la piel');

INSERT INTO medicos (nombre, especialidad_id, consultorio, disponible) VALUES
    ('Dr. Carlos Ramírez', 1, 'Consultorio 101', 1),
    ('Dra. Laura Pérez',   2, 'Consultorio 205', 1),
    ('Dr. Miguel Torres',  3, 'Consultorio 312', 1);

INSERT INTO pacientes (nombre, correo, telefono) VALUES
    ('José Castro',    'jose.castro261299@potros.itson.edu.mx', '6221001001'),
    ('Rosa Gabriela', 'rosa.pina@potros.itson.edu.mx', '6221002002');