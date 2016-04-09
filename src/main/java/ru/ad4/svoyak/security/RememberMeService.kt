package ru.ad4.svoyak.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices
import org.springframework.stereotype.Service
import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class RememberMeService @Inject constructor(
        val userDetailsService: UserDetailsService,

        @Value("\${svoyak.security.remember-me-key}")
        val rememberMyKey: String) : AbstractRememberMeServices(rememberMyKey, userDetailsService) {

    // Token is valid for one year
    private val TOKEN_VALIDITY_DAYS = 365

    private val TOKEN_VALIDITY_SECONDS = 60 * 60 * 24 * TOKEN_VALIDITY_DAYS

    private val DEFAULT_SERIES_LENGTH = 16

    private val DEFAULT_TOKEN_LENGTH = 16

    override fun processAutoLoginCookie(cookieTokens: Array<out String>?, request: HttpServletRequest?, response: HttpServletResponse?): UserDetails? {
        throw UnsupportedOperationException()
    }

    override fun onLoginSuccess(request: HttpServletRequest?, response: HttpServletResponse?, successfulAuthentication: Authentication?) {
        throw UnsupportedOperationException()
    }
}
