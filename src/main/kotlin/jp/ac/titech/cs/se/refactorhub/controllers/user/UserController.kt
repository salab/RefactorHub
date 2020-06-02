package jp.ac.titech.cs.se.refactorhub.controllers.user

import jp.ac.titech.cs.se.refactorhub.services.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/{id}")
    fun get(@PathVariable("id") id: Long) = userService.get(id)

    @GetMapping("/{id}/drafts")
    fun getDrafts(@PathVariable("id") id: Long) = userService.getDrafts(id)

    @GetMapping("/{id}/refactorings")
    fun getRefactorings(@PathVariable("id") id: Long) = userService.getRefactorings(id)

    @GetMapping("/me")
    fun me() = userService.me()
}
