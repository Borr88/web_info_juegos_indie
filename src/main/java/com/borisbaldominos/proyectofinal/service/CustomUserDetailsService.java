package com.borisbaldominos.proyectofinal.service;

import com.borisbaldominos.proyectofinal.model.Usuario;
import com.borisbaldominos.proyectofinal.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Clase CustomUserDetailsService
 * ------------------------------
 * Servicio esencial para la seguridad.
 * Su función es buscar un usuario en NUESTRA base de datos y convertirlo
 * en un objeto 'UserDetails' que Spring Security pueda entender y validar.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Metodo que Spring Security llama automáticamente cuando alguien intenta hacer login.
     * @param username El nombre de usuario escrito en el formulario.
     * @return UserDetails con la info del usuario y sus permisos.
     * @throws UsernameNotFoundException Si el usuario no existe.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Buscamos el usuario en la BD
        Usuario usuario = usuarioRepository.findByNombre(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 2. Convertimos el ROL (Enum) a Autoridad de Spring
        // Usamos el nombre del rol tal cual ("ADMIN" o "USER")
        SimpleGrantedAuthority autoridad = new SimpleGrantedAuthority(usuario.getRol().name());

        // 3. Devolvemos el objeto User de Spring con todos los datos necesarios
        return new User(
                usuario.getNombre(),
                usuario.getPassword(),      // Contraseña encriptada
                usuario.isActivo(),         // Enabled (Activo)
                true,                       // AccountNonExpired
                true,                       // CredentialsNonExpired
                true,                       // AccountNonLocked
                Collections.singletonList(autoridad) // Lista de permisos
        );
    }
}