package ru.ad4.svoyak.security

import com.jcabi.log.Logger
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.ad4.svoyak.data.services.UserService
import ru.ad4.svoyak.utils.mapOrNull
import ru.ad4.svoyak.utils.mapOrThrow
import ru.ad4.svoyak.utils.toList
import javax.inject.Inject

@Service
class UserDetailsService @Inject constructor(
        val userService: UserService) : org.springframework.security.core.userdetails.UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails? {
        Logger.debug(this, "Authenticating $username")

        // находим по логину
        val user = username.mapOrNull { userService.findUserByLogin(it) }

        // возвращаем UserDetails или кидаем exception
        return user.mapOrThrow(
                {
                    org.springframework.security.core.userdetails.User(it.login,
                            it.password,
                            SimpleGrantedAuthority(AuthoritiesConstants.USER).toList())
                },
                UsernameNotFoundException("User $username was not found in the database"))
    }
}