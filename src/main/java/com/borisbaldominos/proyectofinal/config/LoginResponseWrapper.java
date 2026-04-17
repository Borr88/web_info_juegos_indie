package com.borisbaldominos.proyectofinal.config;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;

/**
 * Wrapper para HttpServletResponse que permite capturar el status code
 * y headers antes de que se envíen al cliente.
 */
public class LoginResponseWrapper extends HttpServletResponseWrapper {

    private int status = SC_OK;

    public LoginResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
        super.setStatus(sc);
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.status = sc;
        super.sendError(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.status = sc;
        super.sendError(sc, msg);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        this.status = SC_FOUND;
        super.sendRedirect(location);
    }

    public int getStatus() {
        return status;
    }
}
