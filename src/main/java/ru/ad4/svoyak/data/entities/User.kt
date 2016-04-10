package ru.ad4.svoyak.data.entities

import java.time.LocalDate
import javax.persistence.*

@Entity
data class User(
        @Column(unique = true)
        var login: String = "",
        var password: String = "",

        @Id @GeneratedValue
        var id: Long = 0
)

@Entity
data class AuthToken(
        var tokenValue: String = "",
        var tokenDate: LocalDate = LocalDate.now(),
        var ipAddr: String = "",
        var ua: String = "",

        @ManyToOne
        @JoinColumn(name = "user_id")
        var user: User = User(),

        @Id
        var series: String = ""
)