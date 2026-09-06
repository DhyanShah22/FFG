package com.antarang.cap.security;

import com.antarang.cap.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        return userRepository.findByLoginIdWithRoles(loginId)
                .map(UserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginId));
    }
}
