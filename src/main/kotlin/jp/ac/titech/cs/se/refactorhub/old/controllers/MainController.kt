package jp.ac.titech.cs.se.refactorhub.old.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MainController {

    @GetMapping("/hello")
    fun hello() = "Get Hello"

    @PostMapping("/hello")
    fun postHello() = "Post Hello"
}
