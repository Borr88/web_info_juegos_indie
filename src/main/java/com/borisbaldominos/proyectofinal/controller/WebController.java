package com.borisbaldominos.proyectofinal.controller;

import com.borisbaldominos.proyectofinal.dto.ReviewDTO;
import com.borisbaldominos.proyectofinal.dto.VideojuegoDTO;
import com.borisbaldominos.proyectofinal.model.Notificacion;
import com.borisbaldominos.proyectofinal.model.Review;
import com.borisbaldominos.proyectofinal.model.Usuario;
import com.borisbaldominos.proyectofinal.model.Videojuego;
import com.borisbaldominos.proyectofinal.repository.UsuarioRepository;
import com.borisbaldominos.proyectofinal.service.NotificacionService;
import com.borisbaldominos.proyectofinal.service.ReviewService;
import com.borisbaldominos.proyectofinal.service.UsuarioService;
import com.borisbaldominos.proyectofinal.service.VideojuegoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Clase WebController
 * -------------------
 * Controlador principal que gestiona las vistas públicas y privadas de la web.
 * Se encarga de mostrar el catálogo, detalles, perfil de usuario, favoritos, etc.
 */
@Controller
public class WebController {

    @Autowired
    private VideojuegoService service;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionService notificacionService;

    // ==========================================
    // 1. NAVEGACIÓN PÚBLICA (Catálogo y Detalles)
    // ==========================================

    /**
     * Página de Inicio (Landing Page).
     * Muestra carrusel, novedades, populares y resultados de búsqueda.
     */
    @GetMapping("/")
    public String index(@RequestParam(value = "keyword", required = false) String keyword, Model model) {

        List<VideojuegoDTO> listaPrincipal;

        // Lógica de búsqueda
        if (keyword != null && !keyword.isEmpty()) {
            listaPrincipal = service.buscarPorNombreDTO(keyword);
        } else {
            listaPrincipal = service.listarTodosDTO();
        }

        model.addAttribute("todosLosJuegos", listaPrincipal);

        // Filtro: Populares (Valoración >= 8.0)
        List<VideojuegoDTO> populares = listaPrincipal.stream()
                .filter(j -> j.getValoracionMedia() != null && j.getValoracionMedia() >= 8.0)
                .toList();
        model.addAttribute("populares", populares);

        // Filtro: Novedades (Campo esNovedad = true)
        List<VideojuegoDTO> novedades = listaPrincipal.stream()
                .filter(VideojuegoDTO::isEsNovedad)
                .toList();
        model.addAttribute("novedades", novedades);

        model.addAttribute("keyword", keyword);

        return "index";
    }

    /**
     * Vista de Categorías (Filtro por Género).
     */
    @GetMapping("/categorias")
    public String vistaCategorias(@RequestParam(required = false) String genero, Model model) {

        // 1. Cargar lista de géneros para el desplegable
        List<String> listaGeneros = service.obtenerTodosGeneros();
        model.addAttribute("generos", listaGeneros);

        // 2. Cargar juegos (Filtrados o Todos)
        List<VideojuegoDTO> juegosMostrados;
        if (genero != null && !genero.isEmpty() && !genero.equals("TODOS")) {
            juegosMostrados = service.buscarPorGeneroDTO(genero);
            model.addAttribute("generoSeleccionado", genero);
        } else {
            juegosMostrados = service.listarTodosDTO();
            model.addAttribute("generoSeleccionado", "TODOS");
        }

        model.addAttribute("videojuegos", juegosMostrados);
        return "categorias";
    }

    /**
     * Ficha técnica de un videojuego.
     */
    @GetMapping("/videojuego/{id}")
    public String verDetalle(@PathVariable Long id, Model model, Principal principal) {

        Optional<Videojuego> juego = service.buscarPorId(id);

        if (juego.isPresent()) {
            model.addAttribute("juego", juego.get());

            // Cargar Reseñas
            List<Review> reviews = reviewService.obtenerReviewsPorJuego(id);
            model.addAttribute("reviews", reviews);

            // Comprobar si es favorito (solo si está logueado)
            boolean esFavorito = false;
            if (principal != null) {
                Usuario usuario = getUsuarioLogueado(principal);
                esFavorito = usuarioService.esFavorito(usuario.getId(), id);
            }
            model.addAttribute("esFavorito", esFavorito);

            return "detalle";
        } else {
            return "redirect:/"; // Si no existe, volvemos al inicio
        }
    }

    // ==========================================
    // 2. GESTIÓN DE USUARIO (Perfil, Favoritos, Reviews)
    // ==========================================

    @GetMapping("/perfil")
    public String miPerfil(Model model, Principal principal) {
        model.addAttribute("usuario", getUsuarioLogueado(principal));
        return "perfil";
    }

    @PostMapping("/actualizar-avatar")
    public String actualizarAvatar(@RequestParam("archivo") MultipartFile archivo, Principal principal) {
        if (!archivo.isEmpty()) {
            Usuario usuario = getUsuarioLogueado(principal);
            usuarioService.subirAvatar(usuario.getId(), archivo);
        }
        return "redirect:/perfil";
    }

    @GetMapping("/favoritos")
    public String misFavoritos(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Usuario usuario = getUsuarioLogueado(principal);
        model.addAttribute("videojuegos", usuario.getFavoritos());
        return "favoritos";
    }

    @GetMapping("/toggle-favorito/{juegoId}")
    public String toggleFavorito(@PathVariable Long juegoId, Principal principal) {
        Usuario usuario = getUsuarioLogueado(principal);
        usuarioService.alternarFavorito(usuario.getId(), juegoId);
        return "redirect:/videojuego/" + juegoId; // Recargamos la misma página
    }

    @PostMapping("/guardar-review")
    public String guardarReview(@ModelAttribute ReviewDTO reviewDTO, Principal principal) {
        reviewService.guardarReview(reviewDTO, principal.getName());
        return "redirect:/videojuego/" + reviewDTO.getVideojuegoId();
    }

    // ==========================================
    // 3. FUNCIONALIDADES AVANZADAS (Recomendaciones, Notificaciones)
    // ==========================================

    @GetMapping("/recomendaciones")
    public String recomendaciones(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Usuario usuario = getUsuarioLogueado(principal);
        List<Videojuego> juegosRecomendados = service.obtenerRecomendaciones(usuario.getId());

        model.addAttribute("videojuegos", juegosRecomendados);
        return "recomendaciones";
    }

    @GetMapping("/notificaciones")
    public String verNotificaciones(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Usuario usuario = getUsuarioLogueado(principal);
        List<Notificacion> lista = notificacionService.obtenerMisNotificaciones(usuario.getId());

        model.addAttribute("notificaciones", lista);
        return "notificaciones";
    }

    // ==========================================
    // 4. MÉTODOS PRIVADOS (HELPERS)
    // ==========================================

    /**
     * Recupera el objeto Usuario completo desde la base de datos usando el Principal.
     */
    private Usuario getUsuarioLogueado(Principal principal) {
        return usuarioRepository.findByNombre(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en sesión"));
    }
}