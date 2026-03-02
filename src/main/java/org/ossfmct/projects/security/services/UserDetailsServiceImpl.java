package org.ossfmct.projects.security.services;

import org.ossfmct.projects.security.models.UserDetailsImpl;
import org.ossfmct.projects.users.models.User;
import org.ossfmct.projects.users.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    /**
     * Interface {@link UserRepository} is a layer for accessing users table in database.
     */
    @Autowired
    UserRepository userRepository;

    /**
     * Loads a user from the database by their username and converts it into a {@link UserDetails} object.
     * This method is used by Spring Security for authentication.
     * @param username the username of the user to be retrieved.
     * @return a {@link UserDetails} representation of the user.
     * @throws UsernameNotFoundException if no user is found with the given username.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.error("User Not Found with username: {}", username);
                return new UsernameNotFoundException("User Not Found with username: " + username);
            });
        return UserDetailsImpl.build(user);
    }
}