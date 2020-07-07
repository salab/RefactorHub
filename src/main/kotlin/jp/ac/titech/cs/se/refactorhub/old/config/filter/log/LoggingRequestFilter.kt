package jp.ac.titech.cs.se.refactorhub.old.config.filter.log

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jp.ac.titech.cs.se.refactorhub.old.models.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.util.Date
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class LoggingRequestFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val wrappedRequest = wrap(request)
        filterChain.doFilter(wrappedRequest, response)
        logRequest(wrappedRequest)
    }

    private fun logRequest(request: ContentCachingRequestWrapper) {
        val log = RequestLog(
            getUser(),
            Date(),
            request.method,
            request.requestURI,
            request.queryString ?: "",
            getContent(request)
        )
        logger.info(jacksonObjectMapper().writeValueAsString(log))
    }

    private fun getUser(): String {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return if (principal is User) principal.name else "guest"
    }

    private fun getContent(request: ContentCachingRequestWrapper): String {
        return request.contentAsByteArray.toString(Charsets.UTF_8) ?: "Fuck"
    }

    private fun wrap(request: HttpServletRequest): ContentCachingRequestWrapper =
        if (request is ContentCachingRequestWrapper) request
        else ContentCachingRequestWrapper(request)

    private fun wrap(response: HttpServletResponse): ContentCachingResponseWrapper =
        if (response is ContentCachingResponseWrapper) response
        else ContentCachingResponseWrapper(response)
}
