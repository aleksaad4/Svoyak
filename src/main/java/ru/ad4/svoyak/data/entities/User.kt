package ru.ad4.svoyak.data.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class User(
        @Column(unique = true)
        var login: String = "",
        var password: String = "",

        @Id @GeneratedValue
        var id: Long = 0
)
