package com.example.pawtrack2.model

/**
 * Plain data holder representing a user's Paw Track account.
 */
data class Account(
    val fullName: String,
    val email: String,
    val password: String
)