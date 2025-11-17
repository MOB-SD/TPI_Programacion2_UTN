package prog2int.Main;

import prog2int.Models.EscrituraNotarial;
import prog2int.Service.PropiedadServiceImpl;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import prog2int.Models.Propiedad;

/**
 * Controlador de las operaciones del menú (adaptado de MenuHandler).
 * Gestiona la interacción con el usuario y llama a PropiedadService.
 */
public class MenuHandler {
    private final Scanner scanner;
    private final PropiedadServiceImpl propiedadService;

    public MenuHandler(Scanner scanner, PropiedadServiceImpl propiedadService) {
        if (scanner == null) throw new IllegalArgumentException("Scanner no puede ser null");
        if (propiedadService == null) throw new IllegalArgumentException("PropiedadService no puede ser null");
        this.scanner = scanner;
        this.propiedadService = propiedadService;
    }

    /**
     * Opción 1: Crear Propiedad y Escritura (transaccional).
     */
    public void crearPropiedadConEscritura() {
        try {
            System.out.println("--- Nueva Propiedad ---");
            // Usamos un ID basado en timestamp como en tu proyecto original
            int propId = (int) (System.currentTimeMillis() / 1000); // ID numérico
            
            System.out.print("Padrón Catastral: ");
            String padron = scanner.nextLine().trim();
            System.out.print("Dirección: ");
            String dir = scanner.nextLine().trim();
            System.out.print("Superficie (m2): ");
            double sup = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Destino (RES o COM): ");
            String destino = scanner.nextLine().trim().toUpperCase();
            System.out.print("Antigüedad (años): ");
            int ant = Integer.parseInt(scanner.nextLine().trim());

            Propiedad prop = new Propiedad(propId, false, padron, dir, sup, destino, ant);

            System.out.println("\n--- Escritura Notarial ---");
            System.out.print("¿Desea agregar la escritura ahora? (s/n): ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                int escId = propId + 1; // ID simple
                
                System.out.print("Número de Escritura: ");
                String nroEscr = scanner.nextLine().trim();
                System.out.print("Notaría: ");
                String notaria = scanner.nextLine().trim();
                System.out.print("Tomo: ");
                String tomo = scanner.nextLine().trim();
                System.out.print("Folio: ");
                String folio = scanner.nextLine().trim();
                
                // Usamos java.sql.Date como requiere el nuevo modelo
                Date fecha = Date.valueOf(LocalDate.now()); 
                
                EscrituraNotarial esc = new EscrituraNotarial(escId, false, nroEscr, fecha, notaria, tomo, folio);
                
                // Vinculamos la escritura a la propiedad
                prop.setEscritura(esc);
            }

            propiedadService.insertar(prop);
            System.out.println("✅ Propiedad registrada exitosamente con ID: " + prop.getId());

        } catch (Exception e) {
            System.err.println("Error al crear la propiedad: " + e.getMessage());
        }
    }

    /**
     * Opción 2: Listar todas las Propiedades.
     */
    public void listarPropiedades() {
        try {
            System.out.print("¿Desea (1) listar todas o (2) buscar por padrón? Ingrese opcion: ");
            int subopcion = Integer.parseInt(scanner.nextLine());
            List<Propiedad> propiedades;

            if (subopcion == 1) {
                propiedades = propiedadService.getAll();
            } else if (subopcion == 2) {
                System.out.print("Ingrese texto a buscar en Padrón: ");
                String filtro = scanner.nextLine().trim();
                propiedades = propiedadService.buscarPorPadron(filtro);
            } else {
                System.out.println("Opción inválida.");
                return;
            }

            if (propiedades.isEmpty()) {
                System.out.println("No se encontraron propiedades.");
                return;
            }

            for (Propiedad p : propiedades) {
                System.out.println("ID: " + p.getId() + ", Padrón: " + p.getPadronCatastral() +
                        ", Dirección: " + p.getDireccion() + ", Destino: " + p.getDestino());
                if (p.getEscritura() != null) {
                    System.out.println("   Escritura: " + p.getEscritura().getNroEscritura() +
                            " (Notaría: " + p.getEscritura().getNotaria() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al listar propiedades: " + e.getMessage());
        }
    }

    /**
     * Opción 3: Actualizar Propiedad.
     */
    public void actualizarPropiedad() {
        try {
            System.out.print("ID de la propiedad a actualizar: ");
            int id = Integer.parseInt(scanner.nextLine());
            Propiedad p = propiedadService.getById(id);

            if (p == null) {
                System.out.println("Propiedad no encontrada.");
                return;
            }

            System.out.print("Nuevo Padrón (actual: " + p.getPadronCatastral() + ", Enter para mantener): ");
            String padron = scanner.nextLine().trim();
            if (!padron.isEmpty()) p.setPadronCatastral(padron);

            System.out.print("Nueva Dirección (actual: " + p.getDireccion() + ", Enter para mantener): ");
            String dir = scanner.nextLine().trim();
            if (!dir.isEmpty()) p.setDireccion(dir);

            System.out.print("Nuevo Destino (actual: " + p.getDestino() + ", Enter para mantener): ");
            String destino = scanner.nextLine().trim().toUpperCase();
            if (!destino.isEmpty()) p.setDestino(destino);

            // Lógica para actualizar escritura (si existe)
            if (p.getEscritura() != null) {
                System.out.print("¿Desea actualizar la escritura? (s/n): ");
                if (scanner.nextLine().equalsIgnoreCase("s")) {
                    EscrituraNotarial e = p.getEscritura();
                    System.out.print("Nueva Notaría (actual: " + e.getNotaria() + "): ");
                    String notaria = scanner.nextLine().trim();
                    if (!notaria.isEmpty()) e.setNotaria(notaria);
                    // ...se podrían agregar más campos
                }
            }

            propiedadService.actualizar(p);
            System.out.println("Propiedad actualizada exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al actualizar propiedad: " + e.getMessage());
        }
    }

    /**
     * Opción 4: Eliminar Propiedad (Soft Delete Transaccional).
     */
    public void eliminarPropiedad() {
        try {
            System.out.print("ID de la propiedad a eliminar: ");
            int id = Integer.parseInt(scanner.nextLine());
            propiedadService.eliminar(id);
            System.out.println("Propiedad (y su escritura asociada) eliminada exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al eliminar propiedad: " + e.getMessage());
        }
    }

    /**
     * Opción 5: Buscar Propiedad por ID.
     */
    public void buscarPropiedadPorId() {
        try {
            System.out.print("ID de la propiedad a buscar: ");
            int id = Integer.parseInt(scanner.nextLine());
            Propiedad p = propiedadService.getById(id);
            
            if (p == null) {
                System.out.println("Propiedad no encontrada.");
                return;
            }
            
            System.out.println("--- Ficha de la Propiedad ---");
            System.out.println("ID: " + p.getId());
            System.out.println("Padrón: " + p.getPadronCatastral());
            System.out.println("Dirección: " + p.getDireccion());
            System.out.println("Superficie: " + p.getSuperficieM2() + " m2");
            System.out.println("Destino: " + p.getDestino());
            System.out.println("Antigüedad: " + p.getAntiguedad() + " años");
            
            if (p.getEscritura() != null) {
                EscrituraNotarial e = p.getEscritura();
                System.out.println("\n--- Datos de la Escritura ---");
                System.out.println("ID Escritura: " + e.getId());
                System.out.println("Número: " + e.getNroEscritura());
                System.out.println("Fecha: " + e.getFecha());
                System.out.println("Notaría: " + e.getNotaria());
                System.out.println("Tomo: " + e.getTomo() + ", Folio: " + e.getFolio());
            } else {
                System.out.println("\n(Sin escritura registrada)");
            }

        } catch (Exception e) {
            System.err.println("Error al buscar propiedad: " + e.getMessage());
        }
    }

    /**
     * Opción 6: Listar todas las Escrituras.
     */
    public void listarEscrituras() {
        try {
            List<EscrituraNotarial> escrituras = propiedadService.getEscrituraService().getAll();
            if (escrituras.isEmpty()) {
                System.out.println("No se encontraron escrituras.");
                return;
            }
            for (EscrituraNotarial e : escrituras) {
                System.out.println("ID: " + e.getId() + ", Nro: " + e.getNroEscritura() +
                        ", Notaría: " + e.getNotaria() + ", Fecha: " + e.getFecha());
            }
        } catch (Exception e) {
            System.err.println("Error al listar escrituras: " + e.getMessage());
        }
    }
}