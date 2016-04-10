package ru.ad4.svoyak.web.admin

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.ad4.svoyak.loaders.chgk.ChgkLoader
import javax.inject.Inject


@RestController
@RequestMapping("/admin")
class AdminController @Inject constructor(
        val chgkLoader: ChgkLoader
) {

    @RequestMapping("/loadChgk")
    fun start() {
        chgkLoader.load()
    }
}