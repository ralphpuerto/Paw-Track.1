package com.example.pawtrack2.presenter

import com.example.pawtrack2.contract.SignUpContract
import com.example.pawtrack2.model.Account
import com.example.pawtrack2.model.AccountRepository
import com.example.pawtrack2.model.Validators

class SignUpPresenter(
    private val repository: AccountRepository
) : SignUpContract.Presenter {

    private var view: SignUpContract.View? = null

    override fun attachView(view: SignUpContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun onSignUpClicked(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        val trimmedName = fullName.trim()
        val trimmedEmail = email.trim()

        val nameError = Validators.validateFullName(fullName)
        val emailError = Validators.validateEmail(email)
        val passwordError = Validators.validatePasswordStrength(password)
        val confirmError = Validators.validateConfirmPassword(password, confirmPassword)

        when {
            nameError != null ->
                view?.showError(nameError)

            emailError != null ->
                view?.showError(emailError)

            passwordError != null ->
                view?.showError(passwordError)

            confirmError != null ->
                view?.showError(confirmError)

            repository.isEmailRegistered(trimmedEmail) ->
                view?.showError("An account with this email already exists.")

            else -> {
                repository.saveAccount(
                    Account(fullName = trimmedName, email = trimmedEmail, password = password)
                )
                view?.clearError()
                view?.navigateToLoginAfterSignUp()
            }
        }
    }
}