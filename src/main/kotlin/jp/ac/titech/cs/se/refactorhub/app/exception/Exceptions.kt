package jp.ac.titech.cs.se.refactorhub.app.exception

open class BadRequestException(message: String?) : Exception(message)

open class UnauthorizedException(message: String?) : Exception(message)

open class ForbiddenException(message: String?) : Exception(message)

open class NotFoundException(message: String?) : Exception(message)

open class ConflictException(message: String?) : Exception(message)
