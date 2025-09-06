package com.djeno.lab1.persistence.models;

import com.djeno.lab1.persistence.enums.Privilege;
import com.djeno.lab1.persistence.enums.Role;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@XmlRootElement(name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    @XmlElement
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private Long id;

    @XmlElement
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @XmlTransient
    @Column(name = "password", nullable = false)
    private String password;

    @XmlElement
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @XmlElement
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @XmlTransient
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PaymentMethod> paymentMethods;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Привилегии пользователя
        Set<Privilege> privileges = role.getPrivileges();
        // Конвертируем их в SimpleGrantedAuthority
        Set<SimpleGrantedAuthority> authorities = privileges.stream()
                .map(p -> new SimpleGrantedAuthority(p.name()))
                .collect(Collectors.toSet());
        // Добавляем саму роль, если нужно
        authorities.add(new SimpleGrantedAuthority(role.name()));
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
