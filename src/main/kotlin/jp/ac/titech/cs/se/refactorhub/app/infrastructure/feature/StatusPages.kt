package jp.ac.titech.cs.se.refactorhub.app.infrastructure.feature

import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import jp.ac.titech.cs.se.refactorhub.app.exception.*

data class ErrorResponse(
    val message: String?
)

fun StatusPagesConfig.setExceptions() {
    exception<BadRequestException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message))
    }
    exception<UnauthorizedException> { call, cause ->
        call.respond(HttpStatusCode.Unauthorized, ErrorResponse(cause.message))
    }
    exception<ForbiddenException> { call, cause ->
        call.respond(HttpStatusCode.Forbidden, ErrorResponse(cause.message))
    }
    exception<NotFoundException> { call, cause ->
        call.respond(HttpStatusCode.NotFound, ErrorResponse(cause.message))
    }
    exception<ConflictException> { call, cause ->
        call.respond(HttpStatusCode.Conflict, ErrorResponse(cause.message))
    }
}
