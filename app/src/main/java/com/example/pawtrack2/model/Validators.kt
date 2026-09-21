package com.example.pawtrack2.model

import android.util.Patterns

object Validators {

    private val NAME_REGEX = Regex("^[A-Za-z][A-Za-z .'-]*[A-Za-z]$|^[A-Za-z]$")

    /**
     * Full name: required, 2-50 chars after trimming, letters/spaces/
     * apostrophes/hyphens/periods only, no leading/trailing/double spaces.
     */
    fun validateFullName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() ->
                "Please enter your full name."
            trimmed.length < 2 ->
                "Full name must be at least 2 characters."
            trimmed.length > 50 ->
                "Full name must be under 50 characters."
            trimmed.contains("  ") ->
                "Full name cannot contain consecutive spaces."
            !NAME_REGEX.matches(trimmed) ->
                "Full name can only contain letters, spaces, apostrophes, periods, and hyphens."
            else -> null
        }
    }

    /**
     * Email: required, no whitespace, under 254 chars, matches a
     * standard email address pattern.
     */
    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() ->
                "Please enter your email."
            trimmed.contains(" ") ->
                "Email cannot contain spaces."
            trimmed.length > 254 ->
                "Email is too long."
            !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() ->
                "Please enter a valid email address."
            else -> null
        }
    }

    /**
     * Password strength required for creating/resetting a password:
     * 8-64 chars, no whitespace, at least one uppercase, one lowercase,
     * one digit, and one special character.
     */
    fun validatePasswordStrength(password: String): String? {
        return when {
            password.isEmpty() ->
                "Please enter a password."
            password.contains(" ") ->
                "Password cannot contain spaces."
            password.length < 8 ->
                "Password must be at least 8 characters."
            password.length > 64 ->
                "Password must be under 64 characters."
            !password.any { it.isUpperCase() } ->
                "Password must contain at least one uppercase letter."
            !password.any { it.isLowerCase() } ->
                "Password must contain at least one lowercase letter."
            !password.any { it.isDigit() } ->
                "Password must contain at least one number."
            !password.any { !it.isLetterOrDigit() } ->
                "Password must contain at least one special character (e.g. ! @ # $ %)."
            else -> null
        }
    }

    /** Login only checks that a password was typed; strength is checked at creation time. */
    fun validatePasswordNotBlank(password: String): String? {
        return if (password.isEmpty()) "Please enter your password." else null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? {
        return when {
            confirm.isEmpty() ->
                "Please confirm your password."
            password != confirm ->
                "Passwords do not match."
            else -> null
        }
    }
}