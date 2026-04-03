package com.ventas.auth;

import com.ventas.usuario.Usuario;
import com.ventas.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String userId)
            throws UsernameNotFoundException {

        Long id;
        try {
            id = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException(
                    "ID de usuario inválido: " + userId);
        }

        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado con id: " + userId));

        if (!usuario.activo) {
            throw new UsernameNotFoundException(
                    "Usuario inactivo: " + userId);
        }

        return new User(
                usuario.id.toString(),
                usuario.password,
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + usuario.rol))
        );
    }
}