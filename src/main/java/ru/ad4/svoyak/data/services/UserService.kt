package ru.ad4.svoyak.data.services

import com.jcabi.log.Logger
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import ru.ad4.svoyak.data.entities.User
import ru.ad4.svoyak.data.repositories.UserRepo
import javax.inject.Inject
import javax.transaction.Transactional

@Service
@Transactional
class UserService @Inject constructor(val userRepo: UserRepo) {

    /**
     * Создение нового пользователя c указанным логином и паролем
     */
    @Synchronized
    fun createUser(login: String, password: String): User? {
        Logger.info(this, "Create user with login [$login] and password [$password]")

        // проверяем нет ли ещё пользователя с таким логином
        if (userRepo.findByLogin(login).isEmpty()) {
            // всё хорошо - нет, создаём
            val user = userRepo.save(User(login, password))

            Logger.info(this, "OK, saved user [$user]")
            return user
        } else {
            // user с таким логином уже есть
            Logger.warn(this, "FAIL, user with login [$login] already exists")
            return null
        }
    }

    /**
     * Поиск пользователя по логину
     */
    fun findUserByLogin(login: String): User? {
        Logger.debug(this, "Find user by login [$login]")

        // ищем по логину
        val byLogin = userRepo.findByLogin(login)

        if (byLogin.isEmpty()) {
            // не нашли
            Logger.warn(this, "FAIL, Can't find user by login [$login]")
            return null
        } else {
            Logger.debug(this, "OK, get [${byLogin[0]}] user")
            return byLogin[0]
        }
    }
}