package com.fatec.Pizzaria_Mario.security; // Ou seu pacote de modelo/segurança

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User; // O User do Spring Security
import java.util.Collection;

public class CustomUserDetails extends User {
    private final String nomeCompleto;
    // Você pode adicionar o ID do usuário aqui se precisar acessá-lo via Principal
    private final String userId; 

    public CustomUserDetails(String userId, String username, String password, 
                             Collection<? extends GrantedAuthority> authorities, 
                             String nomeCompleto) {
        super(username, password, authorities);
        this.userId = userId;
        this.nomeCompleto = nomeCompleto;
    }

    // Construtor completo se você precisar dos outros booleanos de status da conta
    public CustomUserDetails(String userId, String username, String password, boolean enabled, 
                             boolean accountNonExpired, boolean credentialsNonExpired, 
                             boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities, 
                             String nomeCompleto) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
        this.nomeCompleto = nomeCompleto;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getUserId() {
        return userId;
    }
}