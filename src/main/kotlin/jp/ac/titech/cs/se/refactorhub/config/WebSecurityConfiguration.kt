package jp.ac.titech.cs.se.refactorhub.config

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import javax.servlet.http.HttpServletResponse

@Configuration
@EnableOAuth2Sso
class WebSecurityConfiguration : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .and()
            .exceptionHandling()
            .authenticationEntryPoint { _, response, _ ->
                response.status = HttpServletResponse.SC_UNAUTHORIZED
            }
            .and()
            .authorizeRequests()
            .mvcMatchers(HttpMethod.POST, "/api/**/*").authenticated()
            .mvcMatchers(HttpMethod.PUT, "/api/**/*").authenticated()
            .mvcMatchers(HttpMethod.PATCH, "/api/**/*").authenticated()
            .mvcMatchers(HttpMethod.DELETE, "/api/**/*").authenticated()
            .and()
            .logout()
            .logoutRequestMatcher(AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/")
            .deleteCookies("JSESSIONID")
            .permitAll()
    }

}
