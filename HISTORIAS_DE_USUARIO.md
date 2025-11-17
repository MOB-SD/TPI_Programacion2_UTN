Aquí tienes el documento de Historias de Usuario completo, adaptado a tu proyecto de Propiedades y EscriturasNotariales.Está basado en el excelente ejemplo que me diste, pero modificado para reflejar la lógica de tu nueva arquitectura (transacciones, borrado lógico en cascada, y la relación 1-a-0..1 entre las entidades).Puedes copiar y pegar esto directamente en un archivo HISTORIAS_DE_USUARIO.md en tu proyecto.Markdown# Historias de Usuario - Sistema de Gestión Inmobiliaria

Especificaciones funcionales completas del sistema CRUD de Propiedades y Escrituras Notariales.

## Tabla de Contenidos

- [Épica 1: Gestión de Propiedades (y Escrituras Asociadas)](#épica-1-gestión-de-propiedades-y-escrituras-asociadas)
- [Épica 2: Consultas y Listados](#épica-2-consultas-y-listados)
- [Reglas de Negocio](#reglas-de-negocio)
- [Modelo de Datos](#modelo-de-datos)

---

## Épica 1: Gestión de Propiedades (y Escrituras Asociadas)

### HU-001: Registrar Propiedad

**Como** usuario del sistema
**Quiero** registrar una nueva propiedad, con la opción de añadir su escritura en el mismo paso
**Para** almacenar nuevos bienes inmuebles de forma transaccional.

#### Criterios de Aceptación

```gherkin
Escenario: Registrar propiedad con escritura (Flujo Transaccional)
  Dado que el usuario selecciona "Registrar Propiedad"
  Cuando ingresa Padrón "P-123", Dirección "Calle Falsa 123", etc.
  Y responde "s" a agregar escritura
  Y ingresa Nro Escritura "E-456", Notaría "Notaría Central", Tomo "I", Folio "10"
  Entonces el sistema inicia una transacción
  Y guarda la Propiedad en la BD
  Y guarda la Escritura Notarial con el ID de la propiedad
  Y confirma la transacción (commit)
  Y muestra "✅ Propiedad registrada exitosamente con ID: X"

Escenario: Registrar propiedad sin escritura
  Dado que el usuario selecciona "Registrar Propiedad"
  Cuando ingresa Padrón "P-789", Dirección "Av. Siempre Viva 742", etc.
  Y responde "n" a agregar escritura
  Entonces el sistema guarda la Propiedad en la BD
  Y no guarda ninguna escritura
  Y muestra "✅ Propiedad registrada exitosamente con ID: X"

Escenario: Falla al registrar la escritura (Rollback)
  Dado que el usuario registra la Propiedad "P-123"
  Y responde "s" a agregar escritura
  Y ingresa un Nro Escritura "E-456" que ya existe (UNIQUE constraint)
  Entonces el sistema intenta guardar la Propiedad (OK)
  Y falla al guardar la Escritura (Error)
  Y el sistema revierte la transacción (rollback)
  Y la Propiedad "P-123" NO queda guardada en la BD
  Y muestra "Error al crear la propiedad: Ya existe una escritura con ese número"

Escenario: Intento de registrar propiedad con Padrón duplicado
  Dado que ya existe una propiedad con Padrón "P-123"
  Cuando el usuario intenta registrar una nueva propiedad con Padrón "P-123"
  Entonces el sistema muestra "Error al crear la propiedad: Ya existe una propiedad con el padrón: P-123"
  Y no crea el registro
Reglas de Negocio AplicablesRN-001: Padrón Catastral, Dirección y Destino son obligatorios.RN-002: El Padrón Catastral debe ser único.RN-003: El Nro de Escritura debe ser único (si se ingresa).RN-004: El alta de Propiedad y Escritura es atómica (transaccional).RN-005: Una Propiedad puede existir sin Escritura (Relación 1 a 0..1).RN-006: Una Escritura no puede existir sin una Propiedad (FK propiedad_id es NOT NULL).Implementación TécnicaClase: MenuHandler.crearPropiedadConEscritura()Servicio: PropiedadServiceImpl.insertar()Validación: PropiedadServiceImpl.validatePropiedad() + validatePadronUnique()Flujo:MenuHandler captura datos y crea objetos Propiedad y EscrituraNotarial.Llama a PropiedadServiceImpl.insertar(propiedad).PropiedadServiceImpl abre una conexión y llama a conn.setAutoCommit(false).Llama a propiedadDAO.insertTx(propiedad, conn).Si propiedad.getEscritura() != null, llama a escrituraService.insertarTx(escritura, conn).Llama a conn.commit().Si algo falla, el catch llama a conn.rollback().HU-002: Actualizar PropiedadComo usuario del sistemaQuiero modificar los datos de una propiedad existente y/o su escrituraPara mantener la información actualizada.Criterios de AceptaciónGherkinEscenario: Actualizar solo datos de la propiedad
  Dado que existe la Propiedad ID 10 (Padrón "P-123")
  Cuando el usuario actualiza la Propiedad ID 10
  Y cambia la Dirección a "Nueva Dirección 456"
  Y presiona Enter para mantener el Padrón
  Y responde "n" a actualizar la escritura
  Entonces el sistema actualiza solo la dirección de la Propiedad ID 10
  Y muestra "Propiedad actualizada exitosamente."

Escenario: Actualizar propiedad y su escritura
  Dado que existe la Propiedad ID 10 con Escritura ID 11 (Notaría "Vieja")
  Cuando el usuario actualiza la Propiedad ID 10
  Y responde "s" a actualizar la escritura
  Y cambia la Notaría a "Notaría Nueva"
  Entonces el sistema actualiza la Propiedad ID 10
  Y actualiza la Escritura ID 11
  Y muestra "Propiedad actualizada exitosamente."

Escenario: Agregar una escritura a una propiedad que no tenía
  Dado que existe la Propiedad ID 12 sin escritura (escritura = null)
  Cuando el usuario actualiza la Propiedad ID 12
  Y el sistema detecta que no hay escritura y pregunta "Desea agregar una?"
  Y el usuario responde "s" y completa los datos
  Entonces el sistema crea la nueva Escritura y la asocia a la Propiedad ID 12
  Y muestra "Propiedad actualizada exitosamente."

Escenario: Actualizar Padrón a uno duplicado
  Dado que existen Propiedad ID 10 (Padrón "P-123") y Propiedad ID 20 (Padrón "P-789")
  Cuando el usuario actualiza la Propiedad ID 20
  Y cambia el Padrón a "P-123"
  Entonces el sistema muestra "Error al actualizar propiedad: Ya existe una propiedad con el padrón: P-123"
  Y no actualiza el registro
Reglas de Negocio AplicablesRN-007: Se valida Padrón Catastral único (excepto para la misma propiedad).RN-008: Campos vacíos (Enter) mantienen valor original.RN-009: Se requiere ID > 0 para actualizar.RN-010: Se puede agregar o actualizar la escritura durante la actualización.Implementación TécnicaClase: MenuHandler.actualizarPropiedad()Servicio: PropiedadServiceImpl.actualizar()Validación: validatePadronUnique(padron, propiedadId)Pattern: if (!padron.isEmpty()) p.setPadronCatastral(padron);HU-003: Eliminar PropiedadComo usuario del sistemaQuiero eliminar una propiedad del sistemaPara mantener solo registros activos.Criterios de AceptaciónGherkinEscenario: Eliminar propiedad con escritura (Transaccional)
  Dado que existe la Propiedad ID 10 con Escritura ID 11
  Cuando el usuario elimina la Propiedad ID 10
  Entonces el sistema inicia una transacción
  Y marca la Escritura ID 11 como `eliminado = TRUE`
  Y marca la Propiedad ID 10 como `eliminado = TRUE`
  Y confirma la transacción (commit)
  Y muestra "Propiedad (y su escritura asociada) eliminada exitosamente."

Escenario: Eliminar propiedad sin escritura
  Dado que existe la Propiedad ID 12 sin escritura
  Cuando el usuario elimina la Propiedad ID 12
  Entonces el sistema marca la Propiedad ID 12 como `eliminado = TRUE`
  Y muestra "Propiedad (y su escritura asociada) eliminada exitosamente."

Escenario: Propiedad eliminada no aparece en listados
  Dado que se eliminó la Propiedad ID 10
  Cuando el usuario lista todas las propiedades
  Entonces la Propiedad ID 10 no aparece en los resultados
Reglas de Negocio AplicablesRN-011: La eliminación es lógica (soft delete), no física.RN-012: Se ejecuta UPDATE ... SET eliminado = TRUE.RN-013: La eliminación de una Propiedad elimina en cascada (lógica) su Escritura asociada.RN-014: La operación es atómica (transaccional).Implementación TécnicaClase: MenuHandler.eliminarPropiedad()Servicio: PropiedadServiceImpl.eliminar()Flujo:PropiedadServiceImpl obtiene la propiedad y su escritura.Abre conexión y llama a conn.setAutoCommit(false).Llama a escrituraService.eliminarTx(escrituraId, conn) (si existe).Llama a propiedadDAO.deleteTx(propiedadId, conn).Llama a conn.commit() (o rollback() si falla).Épica 2: Consultas y ListadosHU-004: Listar y Buscar PropiedadesComo usuario del sistemaQuiero ver un listado de todas las propiedades o buscar por padrónPara consultar la información almacenada.Criterios de AceptaciónGherkinEscenario: Listar todas las propiedades
  Dado que existen propiedades activas
  Cuando el usuario selecciona "Listar todas las Propiedades" y elige "1" (listar todas)
  Entonces el sistema muestra ID, Padrón, Dirección y Destino de cada propiedad
  Y para propiedades CON escritura, muestra "   Escritura: [Nro] (Notaría: [Notaria])"

Escenario: Listar propiedades sin escritura
  Dado que existe una propiedad sin escritura asociada
  Cuando el usuario lista todas las propiedades
  Entonces el sistema muestra la propiedad sin la línea de "Escritura"

Escenario: Buscar por Padrón (LIKE)
  Dado que existen propiedades con Padrón "P-123-A" y "P-123-B"
  Cuando el usuario selecciona "Listar..." y elige "2" (buscar por padrón)
  Y busca por "P-123"
  Entonces el sistema muestra ambas propiedades

Escenario: No hay propiedades
  Dado que no existen propiedades activas
  Cuando el usuario lista todas las propiedades
  Entonces el sistema muestra "No se encontraron propiedades."
Reglas de Negocio AplicablesRN-015: Solo se listan propiedades con eliminado = FALSE.RN-016: La escritura se obtiene mediante LEFT JOIN.RN-017: La búsqueda por padrón es flexible (usa LIKE %filtro%).RN-018: Espacios en la búsqueda se eliminan (trim()).Implementación TécnicaClase: MenuHandler.listarPropiedades()Servicio: PropiedadServiceImpl.getAll() y buscarPorPadron()DAO: PropiedadDAO.getAll() y buscarPorPadron()Query: SELECT p.*, e.* FROM Propiedades p LEFT JOIN EscrituraNotarial e ON p.id = e.propiedad_id WHERE p.eliminado = FALSE.HU-005: Buscar Propiedad por ID (Ficha Detallada)Como usuario del sistemaQuiero ver todos los detalles de una propiedad y su escrituraPara consultar la información completa de un solo bien.Criterios de AceptaciónGherkinEscenario: Ver ficha de propiedad con escritura
  Dado que existe la Propiedad ID 10 con Escritura
  Cuando el usuario selecciona "Buscar Propiedad por ID" e ingresa "10"
  Entonces el sistema muestra una ficha con todos los campos de la Propiedad (ID, Padrón, Dirección, Superficie, etc.)
  Y muestra una sección separada "--- Datos de la Escritura ---" con todos sus campos (Nro, Fecha, Notaría, Tomo, Folio)

Escenario: Ver ficha de propiedad sin escritura
  Dado que existe la Propiedad ID 12 sin escritura
  Cuando el usuario selecciona "Buscar Propiedad por ID" e ingresa "12"
  Entonces el sistema muestra la ficha con todos los datos de la Propiedad
  Y muestra el mensaje "(Sin escritura registrada)"

Escenario: Propiedad no encontrada
  Dado que no existe la Propiedad ID 999
  Cuando el usuario busca por ID "999"
  Entonces el sistema muestra "Propiedad no encontrada."
Reglas de Negocio AplicablesRN-019: Se debe mostrar toda la información de ambas entidades.RN-020: Se obtiene la entidad con getById(id).Implementación TécnicaClase: MenuHandler.buscarPropiedadPorId()Servicio: PropiedadServiceImpl.getById()DAO: PropiedadDAO.getById() (que ya incluye el LEFT JOIN)HU-006: Listar Todas las EscriturasComo usuario del sistemaQuiero ver un listado de todas las escrituras registradasPara auditar o consultar los documentos legales existentes.Criterios de AceptaciónGherkinEscenario: Listar escrituras existentes
  Dado que existen escrituras activas en el sistema
  Cuando el usuario selecciona "Listar todas las Escrituras"
  Entonces el sistema muestra ID, Nro de Escritura, Notaría y Fecha de cada escritura

Escenario: No hay escrituras
  Dado que no existen escrituras activas
  Cuando el usuario lista todas las escrituras
  Entonces el sistema muestra "No se encontraron escrituras."
Reglas de Negocio AplicablesRN-021: Solo se listan escrituras con eliminado = FALSE.Implementación TécnicaClase: MenuHandler.listarEscrituras()Servicio: propiedadService.getEscrituraService().getAll()DAO: EscrituraDAO.getAll()Query: SELECT * FROM EscrituraNotarial WHERE eliminado = FALSE.Reglas de NegocioCódigoReglaImplementaciónRN-001Padrón, Dirección y Destino son obligatoriosPropiedadServiceImpl.validatePropiedad()RN-002Padrón Catastral debe ser únicoPropiedadServiceImpl.validatePadronUnique()RN-003Nro de Escritura debe ser únicoConstraint UNIQUE en BDRN-004Alta de Propiedad y Escritura es atómicaPropiedadServiceImpl.insertar() usa try-catch-rollbackRN-005Una Propiedad puede existir sin EscrituraLEFT JOIN en PropiedadDAORN-006Una Escritura no puede existir sin PropiedadFK propiedad_id es NOT NULLRN-007Validación de Padrón único ignora el ID actualvalidatePadronUnique(padron, propiedadId)RN-008Campos vacíos en actualización mantienen valor originalMenuHandler usa if (!input.isEmpty())RN-011Eliminación es lógica (Soft Delete)UPDATE ... SET eliminado = TRUERN-013Borrado de Propiedad borra su EscrituraPropiedadServiceImpl.eliminar()RN-014Borrado de Propiedad y Escritura es atómicoPropiedadServiceImpl.eliminar() usa try-catch-rollbackRN-015Solo se listan registros no eliminadosWHERE p.eliminado = FALSERN-017Búsqueda por padrón es flexibleLIKE %filtro% en PropiedadDAORN-022Se usan PreparedStatements100% de las queries parametrizadasRN-023Recursos JDBC se cierrantry-with-resources en Config y DaoModelo de DatosDiagrama Entidad-Relación┌─────────────────────────────────┐
│           Propiedades           │
├─────────────────────────────────┤
│ id: INT PK                      │
│ padronCatastral: VARCHAR UNIQUE │
│ direccion: VARCHAR              │
│ superficieM2: DOUBLE            │
│ destino: VARCHAR                │
│ antiguedad: INT                 │
│ eliminado: BOOLEAN              │
└──────────────┬──────────────────┘
               │ 1
               │
               │ (Relación 1 a 0..1)
               │
               │
┌──────────────▼──────────────────┐
│       EscrituraNotarial         │
├─────────────────────────────────┤
│ id: INT PK                      │
│ nroEscritura: VARCHAR UNIQUE    │
│ fecha: DATE                     │
│ notaria: VARCHAR                │
│ tomo: VARCHAR                   │
│ folio: VARCHAR                  │
│ eliminado: BOOLEAN              │
│ propiedad_id: INT FK NOT NULL_UNIQUE │
└─────────────────────────────────┘
Constraints y QueriesSQL-- Constraint de Clave Foránea
ALTER TABLE EscrituraNotarial ADD CONSTRAINT fk_propiedad
  FOREIGN KEY (propiedad_id) REFERENCES Propiedades(id)
  ON DELETE CASCADE;

-- Constraint de Unicidad
ALTER TABLE EscrituraNotarial ADD CONSTRAINT uk_propiedad_id UNIQUE (propiedad_id);

-- Query principal (LEFT JOIN)
SELECT p.*, e.* FROM Propiedades p 
LEFT JOIN EscrituraNotarial e ON p.id = e.propiedad_id 
WHERE p.eliminado = FALSE;
Flujos Técnicos CríticosFlujo 1: Crear Propiedad con Escritura (Transacción)Usuario (MenuHandler)
    ↓ captura datos, crea Propiedad (p) y Escritura (e)
    ↓ p.setEscritura(e)
PropiedadServiceImpl.insertar(p)
    ↓ valida Padrón (OK)
    ↓ try (Connection conn = ...)
        ↓ conn.setAutoCommit(false)
        ↓ try
            ↓ propiedadDAO.insertTx(p, conn)
                ↓ (SQL: INSERT INTO Propiedades...)
            ↓ escrituraService.insertarTx(p.getEscritura(), conn)
                ↓ (SQL: INSERT INTO EscrituraNotarial... VALUES ... p.getId())
            ↓ conn.commit()
        ↓ catch (Exception e)
            ↓ conn.rollback()
            ↓ throw "Error en la transacción"
Usuario recibe: "✅ Propiedad registrada..."
Flujo 2: Eliminar Propiedad (Transacción)Usuario (MenuHandler)
    ↓ ingresa propiedadId
PropiedadServiceImpl.eliminar(propiedadId)
    ↓ p = propiedadDAO.getById(propiedadId) // (Trae la Propiedad y su Escritura)
    ↓ try (Connection conn = ...)
        ↓ conn.setAutoCommit(false)
        ↓ try
            ↓ if (p.getEscritura() != null)
                ↓ escrituraService.eliminarTx(p.getEscritura().getId(), conn)
                    ↓ (SQL: UPDATE EscrituraNotarial SET eliminado=TRUE...)
            ↓ propiedadDAO.deleteTx(propiedadId, conn)
                ↓ (SQL: UPDATE Propiedades SET eliminado=TRUE...)
            ↓ conn.commit()
        ↓ catch (Exception e)
            ↓ conn.rollback()
            ↓ throw "Error en la transacción"
Usuario recibe: "Propiedad (y su escritura asociada) eliminada..."
Resumen de Operaciones del MenúOpciónOperaciónHandlerHU1Registrar Propiedad (con Escritura)crearPropiedadConEscritura()HU-0012Listar todas las PropiedadeslistarPropiedades()HU-0043Actualizar datos de PropiedadactualizarPropiedad()HU-0024Eliminar Propiedad (y su Escritura)eliminarPropiedad()HU-0035Buscar Propiedad por IDbuscarPropiedadPorId()HU-0056Listar todas las EscrituraslistarEscrituras()HU-0060SalirSetea running = false-
