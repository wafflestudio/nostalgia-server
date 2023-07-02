package com.wafflestudio.ggzz.domain.user.repository

import com.wafflestudio.ggzz.domain.user.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findMeById(id: Long): User
    fun existsByUsername(username: String): Boolean
    fun findByUsername(username: String): User?
    fun findUserById(id: Long): User?
}