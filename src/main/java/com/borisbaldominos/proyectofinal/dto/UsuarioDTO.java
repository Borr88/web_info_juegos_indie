package com.borisbaldominos.proyectofinal.dto;

import lombok.Data;
import com.borisbaldominos.proyectofinal.model.Rol;

@Data
public class UsuarioDTO {
    private Long id;
    private String username;
    private String email;
    private Rol rol;

}
