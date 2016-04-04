package ru.ad4.svoyak.data.repositories

import org.springframework.data.jpa.repository.JpaRepository
import ru.ad4.svoyak.data.entities.User

interface UserRepo : JpaRepository<User, Long> {
    fun findByLogin(login: String): List<User>
}



