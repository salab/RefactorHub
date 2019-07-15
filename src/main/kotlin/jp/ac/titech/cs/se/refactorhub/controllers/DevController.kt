package jp.ac.titech.cs.se.refactorhub.controllers

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Profile("dev")
@Controller
class DevController {
    @GetMapping("/")
    fun dummy(): String = "redirect:http://localhost:3000"
}
