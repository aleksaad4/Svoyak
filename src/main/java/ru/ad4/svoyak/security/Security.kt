package ru.ad4.svoyak.security

import com.jcabi.log.Logger
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.access.AccessDeniedHandlerImpl
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.csrf.CsrfException
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Constants for Spring Security authorities.
 */
object AuthoritiesConstants {
    val ADMIN = "ROLE_ADMIN"
    val USER = "ROLE_USER"
    val ANONYMOUS = "ROLE_ANONYMOUS"
}


/**
 * Returns a 401 error code (Unauthorized) to the client, when Ajax authentication fails.
 */
@Service
class AjaxAuthenticationFailureHandler : SimpleUrlAuthenticationFailureHandler() {

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationFailure(request: HttpServletRequest, response: HttpServletResponse,
                                         exception: AuthenticationException) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed")
    }
}

/**
 * Spring Security success handler, specialized for Ajax requests.
 */
@Service
class AjaxAuthenticationSuccessHandler : SimpleUrlAuthenticationSuccessHandler() {

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse,
                                         authentication: Authentication) {
        response.status = HttpServletResponse.SC_OK
    }
}

/**
 * Spring Security logout handler, specialized for Ajax requests.
 */
@Component
class AjaxLogoutSuccessHandler : AbstractAuthenticationTargetUrlRequestHandler(), LogoutSuccessHandler {

    @Throws(IOException::class, ServletException::class)
    override fun onLogoutSuccess(request: HttpServletRequest, response: HttpServletResponse,
                                 authentication: Authentication) {
        response.status = HttpServletResponse.SC_OK
    }
}

/**
 * An implementation of AccessDeniedHandler by wrapping the AccessDeniedHandlerImpl.

 * In addition to sending a 403 (SC_FORBIDDEN) HTTP error code, it will remove the invalid CSRF cookie from the browser
 * side when a CsrfException occurs. In this way the browser side application, e.g. JavaScript code, can
 * distinguish the CsrfException from other AccessDeniedExceptions and perform more specific operations. For instance,
 * send a GET HTTP method to obtain a new CSRF token.

 * @see AccessDeniedHandlerImpl
 */
class CustomAccessDeniedHandler : AccessDeniedHandler {

    private val accessDeniedHandlerImpl = AccessDeniedHandlerImpl()

    @Throws(IOException::class, ServletException::class)
    override fun handle(request: HttpServletRequest, response: HttpServletResponse,
                        accessDeniedException: AccessDeniedException) {

        if (accessDeniedException is CsrfException && !response.isCommitted) {
            // Remove the session cookie so that client knows it's time to obtain a new CSRF token
            val pCookieName = "CSRF-TOKEN"
            val cookie = Cookie(pCookieName, "")
            cookie.maxAge = 0
            cookie.isHttpOnly = false
            cookie.path = "/"
            response.addCookie(cookie)
        }

        accessDeniedHandlerImpl.handle(request, response, accessDeniedException)
    }

    /**
     * The error page to use. Must begin with a "/" and is interpreted relative to the current context root.

     * @param errorPage the dispatcher path to display
     * *
     * *
     * @throws IllegalArgumentException if the argument doesn't comply with the above limitations
     * *
     * @see AccessDeniedHandlerImpl.setErrorPage
     */
    fun setErrorPage(errorPage: String) {
        accessDeniedHandlerImpl.setErrorPage(errorPage)
    }
}

/**
 * Returns a 401 error code (Unauthorized) to the client.
 */
@Component
class Http401UnauthorizedEntryPoint : AuthenticationEntryPoint {

    /**
     * Always returns a 401 error code to the client.
     */
    @Throws(IOException::class, ServletException::class)
    override fun commence(request: HttpServletRequest, response: HttpServletResponse, arg2: AuthenticationException) {
        Logger.debug(this, "Pre-authenticated entry point called. Rejecting access")
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied")
    }
}
