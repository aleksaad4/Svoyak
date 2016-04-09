package ru.ad4.svoyak.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.csrf.CsrfFilter
import ru.ad4.svoyak.security.*
import javax.inject.Inject

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class SecurityConfiguration @Inject constructor(
        val ajaxAuthenticationSuccessHandler: AjaxAuthenticationSuccessHandler,
        val ajaxAuthenticationFailureHandler: AjaxAuthenticationFailureHandler,
        val ajaxLogoutSuccessHandler: AjaxLogoutSuccessHandler,
        val authenticationEntryPoint: Http401UnauthorizedEntryPoint,
        val userDetailsService: UserDetailsService,
        val rememberMeService: RememberMeService,
        @Value("\${svoyak.security.remember-me-key}")
        val rememberMyKey: String) : WebSecurityConfigurerAdapter() {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Inject
    @Throws(Exception::class)
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Throws(Exception::class)
    override fun configure(web: WebSecurity?) {
        web!!.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/app/**/*.{js,css}")
                .antMatchers("/app/**/fonts/**/*")
                .antMatchers("/bower_components/**")
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .csrf()
                .ignoringAntMatchers("/websocket/**")
                .and()
                .addFilterAfter(CsrfCookieGeneratorFilter(), CsrfFilter::class.java)
                .exceptionHandling()
                .accessDeniedHandler(CustomAccessDeniedHandler())
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .rememberMe()
                .rememberMeServices(rememberMeService)
                .rememberMeParameter("remember-me")
                .key(rememberMyKey)
                .and()
                .formLogin()
                .loginProcessingUrl("/api/authentication")
                .successHandler(ajaxAuthenticationSuccessHandler)
                .failureHandler(ajaxAuthenticationFailureHandler)
                .usernameParameter("s_username")
                .passwordParameter("s_password").permitAll()
                .and()
                .logout()
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(ajaxLogoutSuccessHandler).deleteCookies("JSESSIONID", "CSRF-TOKEN").permitAll()
                .and()
                .headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .antMatchers("/api/register").permitAll()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/game/**").hasAuthority(AuthoritiesConstants.USER)
                .antMatchers("/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/api/**").authenticated()

    }
}
