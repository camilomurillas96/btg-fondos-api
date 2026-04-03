CREATE TABLE IF NOT EXISTS Cliente (
    id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    ciudad VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS Sucursal (
    id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ciudad VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS Producto (
    id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipoProducto VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS Inscripcion (
    idProducto INT NOT NULL REFERENCES Producto(id),
    idCliente INT NOT NULL REFERENCES Cliente(id),
    PRIMARY KEY (idProducto, idCliente)
);

CREATE TABLE IF NOT EXISTS Disponibilidad (
    idSucursal INT NOT NULL REFERENCES Sucursal(id),
    idProducto INT NOT NULL REFERENCES Producto(id),
    PRIMARY KEY (idSucursal, idProducto)
);

CREATE TABLE IF NOT EXISTS Visitan (
    idSucursal INT NOT NULL REFERENCES Sucursal(id),
    idCliente INT NOT NULL REFERENCES Cliente(id),
    fechaVisita DATE NOT NULL
);

-- ─────────────────────────────────────────────────────────────
-- DATOS DE EJEMPLO
-- ─────────────────────────────────────────────────────────────

INSERT INTO Cliente VALUES
    (1, 'Ana', 'García López', 'Bogotá'),
    (2, 'Luis', 'Martínez Ruiz', 'Medellín'),
    (3, 'Sofia', 'Pérez Torres', 'Cali');

INSERT INTO Sucursal VALUES
    (1, 'Sucursal Chapinero', 'Bogotá'),
    (2, 'Sucursal Usaquén', 'Bogotá'),
    (3, 'Sucursal El Poblado', 'Medellín');

INSERT INTO Producto VALUES
    (1, 'FPV_RECAUDADORA', 'FPV'),
    (2, 'FDO_ACCIONES', 'FIC'),
    (3, 'DEUDA_PRIVADA', 'FIC');

INSERT INTO Inscripcion VALUES
    (1, 1),
    (2, 2),
    (3, 3);

INSERT INTO Disponibilidad VALUES
    (1, 1),
    (2, 1),
    (1, 2),
    (3, 3);

INSERT INTO Visitan VALUES
    (1, 1, '2024-01-10'),
    (2, 1, '2024-01-11'),
    (1, 2, '2024-01-12'),
    (3, 3, '2024-02-01');

-- ─────────────────────────────────────────────────────────────
-- CONSULTAS PRINCIPALES
-- ─────────────────────────────────────────────────────────────

-- OPCIÓN 1: Doble NOT EXISTS (Alta compatibilidad y rendimiento)
SELECT DISTINCT c.nombre
FROM Cliente c
JOIN Inscripcion i
  ON c.id = i.idCliente
WHERE NOT EXISTS (
    SELECT 1
    FROM Disponibilidad d
    WHERE d.idProducto = i.idProducto
      AND NOT EXISTS (
          SELECT 1
          FROM Visitan v
          WHERE v.idSucursal = d.idSucursal
            AND v.idCliente = c.id
      )
);

-- OPCIÓN 2: Uso de EXCEPT
SELECT DISTINCT c.nombre
FROM Cliente c
JOIN Inscripcion i
  ON c.id = i.idCliente
WHERE NOT EXISTS (
    SELECT idSucursal
    FROM Disponibilidad
    WHERE idProducto = i.idProducto

    EXCEPT

    SELECT idSucursal
    FROM Visitan
    WHERE idCliente = c.id
);

-- OPCIÓN 3: Agrupación y Conteo (La más descriptiva "legible para humanos")
-- "Mismo número de sucursales disponibles que de sucursales visitadas"
SELECT c.nombre
FROM Cliente c
JOIN Inscripcion i
  ON c.id = i.idCliente
JOIN Disponibilidad d
  ON i.idProducto = d.idProducto
LEFT JOIN Visitan v
  ON v.idSucursal = d.idSucursal
 AND v.idCliente = c.id
GROUP BY
    c.id,
    c.nombre,
    i.idProducto
HAVING COUNT(d.idSucursal) = COUNT(v.idSucursal);
