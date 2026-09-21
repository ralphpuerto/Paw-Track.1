package com.example.pawtrack2.model

import android.content.Context

/**
 * Abstraction over account persistence. This is the Model layer in MVP:
 * presenters depend on this interface, never on SharedPreferences directly,
 * so the persistence mechanism can be swapped (e.g. for a database or a
 * fake in tests) without touching any presenter or view code.
 */
interface AccountRepository {
    fun hasAccount(): Boolean
    fun getAccount(): Account?
    fun saveAccount(account: Account)
    fun updatePassword(newPassword: String)
    fun isEmailRegistered(email: String): Boolean
    fun validateCredentials(email: String, password: String): Boolean
}

class SharedPreferencesAccountRepository(context: Context) : AccountRepository {

    private val preferences =
        context.applicationContext.getSharedPreferences("pawtrack_account", Context.MODE_PRIVATE)

    override fun hasAccount(): Boolean = preferences.contains("email")

    override fun getAccount(): Account? {
        val email = preferences.getString("email", null) ?: return null
        val fullName = preferences.getString("fullName", "") ?: ""
        val password = preferences.getString("password", "") ?: ""
        return Account(fullName = fullName, email = email, password = password)
    }

    override fun saveAccount(account: Account) {
        preferences.edit()
            .putString("fullName", account.fullName)
            .putString("email", account.email)
            .putString("password", account.password)
            .apply()
    }

    override fun updatePassword(newPassword: String) {
        preferences.edit()
            .putString("password", newPassword)
            .apply()
    }

    override fun isEmailRegistered(email: String): Boolean {
        val existing = preferences.getString("email", null) ?: return false
        return existing.equals(email, ignoreCase = true)
    }

    override fun validateCredentials(email: String, password: String): Boolean {
        val account = getAccount() ?: return false
        return account.email.equals(email, ignoreCase = true) && account.password == password
    }
}