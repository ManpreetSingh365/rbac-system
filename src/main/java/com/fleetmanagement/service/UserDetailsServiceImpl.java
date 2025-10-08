package com.fleetmanagement.service;

import com.fleetmanagement.dto.response.UserLoginResponse;
import com.fleetmanagement.entity.User;
import com.fleetmanagement.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User Details Service Implementation
 * Loads user details for Spring Security authentication
 * Updated to use CustomUserDetails with UUID
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);

        User user = userRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return buildUserPrincipal(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return buildUserPrincipal(user);
    }

    /**
     * Build CustomUserDetails from User entity
     */
    private UserDetails buildUserPrincipal(User user) {
        // Get all user permissions for authorities
        Set<String> permissions = permissionService.getAllUserPermissions(user.getId());

        Collection<GrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        log.debug("User {} loaded with {} authorities", user.getUsername(), authorities.size());

        return new UserLoginResponse(
                user.getId(),                  // UUID id
                user.getUsername(),            // username
                user.getPassword(),
                user.getTenantId(),            // password
                authorities                    // roles/permissions
        );
    }
}
