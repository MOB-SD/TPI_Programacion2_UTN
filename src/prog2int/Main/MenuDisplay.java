package prog2int.Main;

/**
 * Clase utilitaria para mostrar el menú (adaptada).
 */
public class MenuDisplay {
    
    public static void mostrarMenuPrincipal() {
        System.out.println("\n========= GESTIÓN INMOBILIARIA =========");
        System.out.println("1. Registrar Propiedad (con Escritura)");
        System.out.println("2. Listar todas las Propiedades");
        System.out.println("3. Actualizar datos de Propiedad");
        System.out.println("4. Eliminar Propiedad (y su Escritura)");
        System.out.println("5. Buscar Propiedad por ID");
        System.out.println("6. Listar todas las Escrituras");
        System.out.println("0. Salir");
        System.out.print("Ingrese una opcion: ");
    }
}