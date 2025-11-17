package prog2int.Main;

import prog2int.Dao.EscrituraDAO;
import prog2int.Dao.PropiedadDAO;
import prog2int.Service.EscrituraServiceImpl;
import prog2int.Service.PropiedadServiceImpl;
import java.util.Scanner;

/**
 * Orquestador principal del menú.
 * Inicializa y conecta todas las capas.
 */
public class AppMenu {
    private final Scanner scanner;
    private final MenuHandler menuHandler;
    private boolean running;

    public AppMenu() {
        this.scanner = new Scanner(System.in);
        // Inyección de dependencias manual
        PropiedadServiceImpl propiedadService = createPropiedadService();
        this.menuHandler = new MenuHandler(scanner, propiedadService);
        this.running = true;
    }

    public static void main(String[] args) {
        AppMenu app = new AppMenu();
        app.run();
    }

    public void run() {
        while (running) {
            try {
                MenuDisplay.mostrarMenuPrincipal();
                int opcion = Integer.parseInt(scanner.nextLine());
                processOption(opcion);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
            }
        }
        scanner.close();
    }

    private void processOption(int opcion) {
        switch (opcion) {
            case 1 -> menuHandler.crearPropiedadConEscritura();
            case 2 -> menuHandler.listarPropiedades();
            case 3 -> menuHandler.actualizarPropiedad();
            case 4 -> menuHandler.eliminarPropiedad();
            case 5 -> menuHandler.buscarPropiedadPorId();
            case 6 -> menuHandler.listarEscrituras();
            case 0 -> {
                System.out.println("Saliendo...");
                running = false;
            }
            default -> System.out.println("Opción no válida.");
        }
    }

    /**
     * Factory Method que crea la cadena de dependencias.
     */
    private PropiedadServiceImpl createPropiedadService() {
        // 1. Crear DAOs
        EscrituraDAO escrituraDAO = new EscrituraDAO();
        PropiedadDAO propiedadDAO = new PropiedadDAO(); // No necesita el otro DAO
        
        // 2. Crear Services
        EscrituraServiceImpl escrituraService = new EscrituraServiceImpl(escrituraDAO);
        PropiedadServiceImpl propiedadService = new PropiedadServiceImpl(propiedadDAO, escrituraService);
        
        return propiedadService;
    }
}