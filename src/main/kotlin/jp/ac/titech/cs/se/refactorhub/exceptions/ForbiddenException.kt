package jp.ac.titech.cs.se.refactorhub.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class ForbiddenException(message: String) : RuntimeException(message)
