package com.borisbaldominos.proyectofinal.config;

import com.borisbaldominos.proyectofinal.model.Rol;
import com.borisbaldominos.proyectofinal.model.Usuario;
import com.borisbaldominos.proyectofinal.model.Videojuego;
import com.borisbaldominos.proyectofinal.repository.UsuarioRepository;
import com.borisbaldominos.proyectofinal.repository.VideojuegoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Clase DataSeeder
 * -----------------
 * Se ejecuta automáticamente al arrancar la aplicación.
 * Su función es pre-cargar datos iniciales en la base de datos (Juegos y Usuario Admin)
 * para que la aplicación no esté vacía la primera vez que se inicia.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private VideojuegoRepository videojuegoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("⏳ Iniciando carga de datos de prueba...");

        // SECCIÓN 1: Carga de Videojuegos
        cargarVideojuegos();

        // SECCIÓN 2: Creación del Usuario Administrador
        crearUsuarioAdmin();

        System.out.println("✅ Carga de datos completada con éxito.");
    }

    // ==========================================
    // METODO 1: CARGA DE JUEGOS
    // ==========================================
    private void cargarVideojuegos() {
        // --- JUEGOS CLÁSICOS (NO NOVEDAD) ---
        crearOActualizarJuego(
                "Hollow Knight",
                "Un caballero sin nombre desafía las profundidades de Hallownest...",
                9.8,
                "https://image.api.playstation.com/vulcan/img/rnd/202010/3017/MPluk22e03Zt7sZkC8F6tZjV.png",
                "Metroidvania",
                false
        );

        crearOActualizarJuego(
                "Stardew Valley",
                "Heredas la vieja granja de tu abuelo...",
                9.5,
                "https://image.api.playstation.com/vulcan/img/rnd/202010/1614/37402f0e-7440-4e3a-8646-633852033626.png",
                "Simulación",
                false
        );

        crearOActualizarJuego(
                "Celeste",
                "Ayuda a Madeline a sobrevivir a sus demonios internos...",
                9.7,
                "https://image.api.playstation.com/vulcan/img/rnd/202010/2119/b41c0985-7848-4796-9817-640103761741.png",
                "Plataformas",
                false
        );

        // --- JUEGOS NOVEDAD (APARECEN EN 'PRÓXIMOS LANZAMIENTOS') ---
        crearOActualizarJuego(
                "Hollow Knight: Silksong",
                "Juega como Hornet, la princesa protectora de Hallownest...",
                0.0, // Nota 0 porque no ha salido
                "https://assets.nintendo.com/image/upload/ar_16:9,c_lpad,w_1240/b_white/f_auto/q_auto/ncom/software/switch/70010000019687/849319d675685a065b9b9426f49dc17855074217122180590a3c20c025555c91",
                "Metroidvania",
                true // Es novedad
        );

        crearOActualizarJuego(
                "Hades II",
                "Lucha más allá del Inframundo empleando hechicería oscura...",
                0.0,
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1145350/header.jpg",
                "Roguelike",
                true // Es novedad
        );
    }

    // ==========================================
    // METODO 2: CREACIÓN DE ADMIN
    // ==========================================
    private void crearUsuarioAdmin() {
        // Comprobamos si ya existe para no duplicarlo ni machacarlo
        if (usuarioRepository.findByNombre("Admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setEmail("admin@indielovers.com");

            // IMPORTANTE: La contraseña siempre debe guardarse encriptada
            admin.setPassword(passwordEncoder.encode("admin123"));

            admin.setRol(Rol.ADMIN); // Rol con permisos totales
            admin.setActivo(true);

            usuarioRepository.save(admin);
            System.out.println("👮 Usuario ADMIN creado: (User: Admin / Pass: admin123)");
        } else {
            System.out.println("ℹ️ El usuario ADMIN ya existe, no se ha modificado.");
        }
    }

    // ==========================================
    // METODO HELPER (AUXILIAR)
    // ==========================================
    /**
     * Busca un juego por título.
     * - Si no existe: Lo crea nuevo.
     * - Si existe: Actualiza sus datos (útil si cambiaste algo en el código y quieres que se refleje en BD).
     */
    private void crearOActualizarJuego(String titulo, String sinopsis, Double valoracion, String portada, String genero, boolean esNovedad) {
        List<Videojuego> existentes = videojuegoRepository.findByTituloContainingIgnoreCase(titulo);

        if (existentes.isEmpty()) {
            // CASO A: CREAR NUEVO
            Videojuego nuevo = new Videojuego();
            nuevo.setTitulo(titulo);
            nuevo.setSinopsis(sinopsis);
            nuevo.setValoracionMedia(valoracion);
            nuevo.setPortadaUrl(portada);
            nuevo.setGenero(genero);
            nuevo.setEsNovedad(esNovedad);
            videojuegoRepository.save(nuevo);
            System.out.println("➕ Juego creado: " + titulo);
        } else {
            // CASO B: ACTUALIZAR EXISTENTE
            Videojuego existente = existentes.get(0);

            // Solo guardamos si hay cambios reales para optimizar
            if (!existente.getGenero().equals(genero) || existente.isEsNovedad() != esNovedad) {
                existente.setGenero(genero);
                existente.setEsNovedad(esNovedad);
                videojuegoRepository.save(existente);
                System.out.println("🔄 Juego actualizado: " + titulo);
            }
        }
    }
}