package com.example.pawtrack2.presenter

import com.example.pawtrack2.contract.LoginContract
import com.example.pawtrack2.model.AccountRepository
import com.example.pawtrack2.model.Validators

class LoginPresenter(
    private val repository: AccountRepository
) : `LoginContract`.Presenter {

    private var view: LoginContract.View? = null

    override fun attachView(view: `LoginContract`.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun onLoginClicked(email: String, password: String) {
        val trimmedEmail = email.trim()

        val emailError = Validators.validateEmail(email)
        val passwordError = Validators.validatePasswordNotBlank(password)

        when {
            emailError != null ->
                view?.showError(emailError)

            passwordError != null ->
                view?.showError(passwordError)

            !repository.hasAccount() ->
                view?.showError("No account found. Please create an account first.")

            !repository.validateCredentials(trimmedEmail, password) ->
                view?.showError("Incorrect email or password.")

            else -> {
                view?.clearError()
                view?.navigateToDashboard()
            }
        }
    }
}