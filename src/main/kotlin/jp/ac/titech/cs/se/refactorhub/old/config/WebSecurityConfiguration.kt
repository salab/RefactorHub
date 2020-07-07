package jp.ac.titech.cs.se.refactorhub.old.config

import jp.ac.titech.cs.se.refactorhub.old.config.filter.GitHubFilter
import jp.ac.titech.cs.se.refactorhub.old.services.UserService
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import javax.servlet.http.HttpServletResponse

@EnableWebSecurity
class WebSecurityConfiguration(
    private val userService: UserService
) : WebSecurityConfigurerAdapter() {

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/_nuxt/**", "/favicon.ico")
    }

    override fun configure(http: HttpSecurity) {
        http.csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .logout().disable()
            .anonymous().disable()
            .exceptionHandling()
            .authenticationEntryPoint { _, response, exception ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.message)
            }
            .and()
            .addFilter(
                GitHubFilter().apply {
                    setAuthenticationManager(authenticationManager())
                }
            )
            .authorizeRequests()
            .mvcMatchers(HttpMethod.POST, "/api/**/*").authenticated()
            .mvcMatchers(HttpMethod.PUT, "/api/**/*").authenticated()
            .mvcMatchers(HttpMethod.PATCH, "/api/**/*").authenticated()
            .mvcMatchers(HttpMethod.DELETE, "/api/**/*").authenticated()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(
            PreAuthenticatedAuthenticationProvider().apply {
                setPreAuthenticatedUserDetailsService(userService)
            }
        )
    }
}
