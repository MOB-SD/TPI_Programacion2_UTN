Sistema de Gestión Inmobiliaria (Propiedades y Escrituras)
Trabajo Práctico Integrador - Programación 2
Descripción del Proyecto
Este proyecto es una aplicación de consola para la gestión de bienes inmuebles, permitiendo registrar propiedades y sus escrituras notariales asociadas.

El sistema está construido sobre una arquitectura robusta de 4 capas (Presentación, Servicio, Acceso a Datos y Modelo) adaptada del proyecto prog2int. Demuestra el manejo de transacciones JDBC para operaciones complejas (como registrar una propiedad y su escritura de forma atómica), el uso del patrón DAO, y una clara separación de responsabilidades.

Características Principales
Gestión Transaccional: El alta de una Propiedad y su Escritura Notarial se maneja en una única transacción. Si una falla, ambas se revierten (rollback).

Arquitectura en Capas: Clara separación entre la Interfaz de Usuario (Main), Lógica de Negocio (Service) y Acceso a Datos (Dao).

Modelo de Dominio Fuerte: Clases Propiedad y EscrituraNotarial que heredan de una EntidadBase común.

Búsqueda Avanzada: Permite listar todo y buscar propiedades por su padrón catastral.

Seguridad: Uso de PreparedStatement en todas las operaciones de base de datos para prevenir inyección SQL.

Soft Delete (Borrado Lógico): Los registros nunca se borran físicamente. Se marcan con eliminado = true, preservando la integridad de los datos.

Tecnologías Utilizadas
Lenguaje: Java

Base de Datos: MySQL

Conexión: JDBC (a través del driver mysql-connector-j)

Arquitectura: 4 Capas (UI, Servicio, DAO, Modelo)

Instalación y Configuración
1. Script de la Base de Datos
Antes de ejecutar el proyecto, necesitas crear las tablas en tu base de datos MySQL. El proyecto está configurado para conectarse a la base de datos tpiprogbd.

Ejecuta el siguiente script en tu gestor de MySQL:

SQL

CREATE DATABASE IF NOT EXISTS tpiprogbd;
USE tpiprogbd;

-- 1. Tabla de Propiedades
CREATE TABLE IF NOT EXISTS Propiedades (
    id INT PRIMARY KEY,
    padronCatastral VARCHAR(100) NOT NULL UNIQUE,
    direccion VARCHAR(255) NOT NULL,
    superficieM2 DOUBLE,
    destino VARCHAR(50),
    antiguedad INT,
    eliminado BOOLEAN DEFAULT FALSE
);

-- 2. Tabla de Escrituras Notariales
CREATE TABLE IF NOT EXISTS EscrituraNotarial (
    id INT PRIMARY KEY,
    nroEscritura VARCHAR(100) NOT NULL UNIQUE,
    fecha DATE,
    notaria VARCHAR(150),
    tomo VARCHAR(50),
    folio VARCHAR(50),
    eliminado BOOLEAN DEFAULT FALSE,
    
    -- Clave Foránea (Relación 1 a 1)
    propiedad_id INT NOT NULL UNIQUE,
    
    FOREIGN KEY (propiedad_id) 
        REFERENCES Propiedades(id)
        ON DELETE CASCADE
);
2. Configuración de la Conexión
La configuración de la base de datos se encuentra en el archivo Config/DatabaseConnection.java.

Asegúrate de que los valores coincidan con tu servidor MySQL local:

Java

public final class DatabaseConnection {
    // Valores por defecto
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/tpiprogbd";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "1234"; // ¡CAMBIA ESTO POR TU CONTRASEÑA!
    
    // ...
}
3. Librería JDBC
Asegúrate de que tu proyecto en NetBeans tenga la librería mysql-connector-j.jar añadida en la carpeta "Libraries" / "Bibliotecas".

Arquitectura del Proyecto
El sistema está dividido en 4 paquetes principales que representan sus capas:

src/
├── Config/
│   └── DatabaseConnection.java   # Configura y provee la conexión JDBC
│
├── Models/
│   ├── EntidadBase.java          # Clase abstracta con 'id' y 'eliminado'
│   ├── Propiedad.java            # Modelo para la tabla Propiedades
│   └── EscrituraNotarial.java    # Modelo para la tabla EscrituraNotarial
│
├── Dao/
│   ├── GenericDAO.java           # Interfaz CRUD genérica
│   ├── PropiedadDAO.java         # Lógica SQL para Propiedades (con LEFT JOIN)
│   └── EscrituraDAO.java         # Lógica SQL para Escrituras
│
├── Service/
│   ├── GenericService.java       # Interfaz de servicio genérica
│   ├── EscrituraServiceImpl.java # Lógica de negocio para Escrituras
│   └── PropiedadServiceImpl.java # Lógica de negocio y TRANSACCIONES
│
└── Main/
    ├── Main.java                 # Punto de entrada (inicia AppMenu)
    ├── AppMenu.java              # Orquesta el menú e inyecta dependencias
    ├── MenuDisplay.java          # Muestra las opciones del menú
    └── MenuHandler.java          # Recibe la entrada del usuario y llama a los servicios
Funcionalidad del Menú
Al ejecutar Main/Main.java, se presentará el siguiente menú:

========= GESTIÓN INMOBILIARIA =========
1. Registrar Propiedad (con Escritura)
2. Listar todas las Propiedades
3. Actualizar datos de Propiedad
4. Eliminar Propiedad (y su Escritura)
5. Buscar Propiedad por ID
6. Listar todas las Escrituras
0. Salir
Ingrese una opcion: 
Explicación de las Opciones
1. Registrar Propiedad (con Escritura)

Pide los datos de la Propiedad (Padrón, Dirección, etc.).

Pregunta si se desea añadir una Escritura ahora.

Si se añade, pide los datos de la escritura.

Llama a PropiedadServiceImpl.insertar(), que guarda AMBOS registros en una sola transacción. Si la escritura falla, la propiedad tampoco se guarda.

2. Listar todas las Propiedades

Permite listar todas las propiedades o buscar por Padrón Catastral (con LIKE).

Gracias al LEFT JOIN en PropiedadDAO, muestra los datos de la escritura si esta existe.

3. Actualizar datos de Propiedad

Pide un ID y permite modificar los datos de la propiedad (Padrón, Dirección...).

También permite actualizar los datos de la escritura asociada.

Valida que el nuevo Padrón Catastral no esté duplicado.

4. Eliminar Propiedad (y su Escritura)

Pide un ID y realiza un borrado lógico (soft delete).

Se ejecuta en una transacción: marca eliminado = true en la Propiedad y también en su EscrituraNotarial asociada.

5. Buscar Propiedad por ID

Muestra una "Ficha" detallada de una sola propiedad y su escritura asociada.

6. Listar todas las Escrituras

Muestra una lista simple de todas las escrituras (no eliminadas) que existen en el sistema.
