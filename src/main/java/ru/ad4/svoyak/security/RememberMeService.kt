package ru.ad4.svoyak.security

import com.jcabi.log.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataAccessException
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.codec.Base64
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices
import org.springframework.security.web.authentication.rememberme.CookieTheftException
import org.springframework.security.web.authentication.rememberme.InvalidCookieException
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException
import org.springframework.stereotype.Service
import ru.ad4.svoyak.data.entities.AuthToken
import ru.ad4.svoyak.data.services.UserService
import ru.ad4.svoyak.loaders.getStackTrace
import ru.ad4.svoyak.utils.mapOrThrow
import java.security.SecureRandom
import java.time.LocalDate
import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class RememberMeService @Inject constructor(
        val userDetailsService: UserDetailsService,
        val userService: UserService,

        @Value("\${svoyak.security.remember-me-key}")
        val rememberMyKey: String) : AbstractRememberMeServices(rememberMyKey, userDetailsService) {

    // Token is valid for one year
    private val TOKEN_VALIDITY_DAYS = 365L

    private val TOKEN_VALIDITY_SECONDS = 60 * 60 * 24 * TOKEN_VALIDITY_DAYS

    private val DEFAULT_SERIES_LENGTH = 16

    private val DEFAULT_TOKEN_LENGTH = 16

    val random: SecureRandom = SecureRandom()

    override fun processAutoLoginCookie(cookieTokens: Array<String>?, request: HttpServletRequest?, response: HttpServletResponse?): UserDetails? {
        val token = getToken(cookieTokens!!)

        // Token also matches, so login is valid. Update the token value, keeping the *same* series number.
        Logger.debug(this, "Refreshing persistent login token for user '${token.user.login}', series '${token.series}'")
        token.tokenDate = LocalDate.now()
        token.tokenValue = generateTokenData()
        token.ipAddr = request!!.remoteAddr
        token.ua = request.getHeader("User-Agent")

        try {
            val savedToken = userService.saveAuthToken(token)
            addCookie(savedToken, request, response!!)
        } catch (e: DataAccessException) {
            Logger.error(this, "Failed to update token, catch exception [${e.getStackTrace()}]", e)
            throw RememberMeAuthenticationException("Autologin failed due to data access problem", e)
        }

        return getUserDetailsService().loadUserByUsername(token.user.login)
    }

    override fun onLoginSuccess(request: HttpServletRequest?, response: HttpServletResponse?, successfulAuthentication: Authentication?) {
        val login = successfulAuthentication!!.name

        Logger.debug(this, "Creating new persistent login for user $login")

        val token = userService.findUserByLogin(login).mapOrThrow({
            AuthToken(generateTokenData(), LocalDate.now(), request!!.remoteAddr, request.getHeader("User-Agent"),
                    it, generateSeriesData())
        }, UsernameNotFoundException("User $login was not found in the database"))

        try {
            val savedToken = userService.saveAuthToken(token)
            addCookie(savedToken, request!!, response!!)
        } catch (e: DataAccessException) {
            Logger.error(this, "Failed to save persistent token, catch exception [${e.getStackTrace()}]", e)
        }

    }

    /**
     * Validate the token and return it
     */
    private fun getToken(cookieTokens: Array<String>): AuthToken {
        if (cookieTokens.size != 2) {
            throw InvalidCookieException("Cookie token did not contain 2 tokens, but contained [$cookieTokens]")
        }
        val presentedSeries = cookieTokens[0]
        val presentedToken = cookieTokens[1]

        // No series match, so we can't authenticate using this cookie
        val token = userService.findAuthToken(presentedSeries) ?: throw RememberMeAuthenticationException("No persistent token found for series id: " + presentedSeries)

        // We have a match for this user/series combination
        Logger.info(this, "PresentedToken=$presentedToken / tokenValue=${token.tokenValue}")

        if (presentedToken != token.tokenValue) {
            // Token doesn't match series value. Delete this session and throw an exception.
            userService.removeAuthToken(token)
            throw CookieTheftException("Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack.")
        }

        if (token.tokenDate.plusDays(TOKEN_VALIDITY_DAYS).isBefore(LocalDate.now())) {
            userService.removeAuthToken(token)
            throw RememberMeAuthenticationException("Remember-me login has expired")
        }

        return token
    }

    private fun generateSeriesData(): String {
        val newSeries = ByteArray(DEFAULT_SERIES_LENGTH)
        random.nextBytes(newSeries)
        return String(Base64.encode(newSeries))
    }

    private fun generateTokenData(): String {
        val newToken = ByteArray(DEFAULT_TOKEN_LENGTH)
        random.nextBytes(newToken)
        return String(Base64.encode(newToken))
    }

    private fun addCookie(token: AuthToken, request: HttpServletRequest, response: HttpServletResponse) {
        setCookie(arrayOf(token.series, token.tokenValue), TOKEN_VALIDITY_SECONDS.toInt(), request, response)
    }
}
