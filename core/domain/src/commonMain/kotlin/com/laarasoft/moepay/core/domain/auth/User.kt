package com.laarasoft.moepay.core.domain.auth

data class User(
    val id: String,
    val phoneNumber: String,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
)
