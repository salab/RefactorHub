package jp.ac.titech.cs.se.refactorhub.app.infrastructure.feature

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import jp.ac.titech.cs.se.refactorhub.app.exception.*

data class ErrorResponse(
    val message: String?
)

fun StatusPages.Configuration.setExceptions() {
    exception<BadRequestException> {
        call.respond(HttpStatusCode.BadRequest, ErrorResponse(it.message))
    }
    exception<UnauthorizedException> {
        call.respond(HttpStatusCode.Unauthorized, ErrorResponse(it.message))
    }
    exception<ForbiddenException> {
        call.respond(HttpStatusCode.Forbidden, ErrorResponse(it.message))
    }
    exception<NotFoundException> {
        call.respond(HttpStatusCode.NotFound, ErrorResponse(it.message))
    }
    exception<ConflictException> {
        call.respond(HttpStatusCode.Conflict, ErrorResponse(it.message))
    }
}
