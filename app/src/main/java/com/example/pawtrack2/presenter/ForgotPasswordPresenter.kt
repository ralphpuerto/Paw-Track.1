package com.example.pawtrack2.presenter

import com.example.pawtrack2.contract.ForgotPasswordContract
import com.example.pawtrack2.model.AccountRepository
import com.example.pawtrack2.model.Validators

class ForgotPasswordPresenter(
    private val repository: AccountRepository
) : `ForgotPasswordContract`.Presenter {

    private var view: `ForgotPasswordContract`.View? = null

    override fun attachView(view: ForgotPasswordContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun onVerifyEmailClicked(email: String) {
        val trimmedEmail = email.trim()
        val emailError = Validators.validateEmail(email)
        val account = repository.getAccount()

        when {
            emailError != null ->
                view?.showError(emailError)

            account == null ->
                view?.showError("No account found. Please create an account first.")

            !account.email.equals(trimmedEmail, ignoreCase = true) ->
                view?.showError("No account found with that email.")

            else -> {
                view?.clearError()
                view?.advanceToPasswordStep(trimmedEmail)
            }
        }
    }

    override fun onResetPasswordClicked(newPassword: String, confirmPassword: String) {
        val passwordError = Validators.validatePasswordStrength(newPassword)
        val confirmError = Validators.validateConfirmPassword(newPassword, confirmPassword)
        val currentPassword = repository.getAccount()?.password ?: ""

        when {
            passwordError != null ->
                view?.showError(passwordError)

            confirmError != null ->
                view?.showError(confirmError)

            newPassword == currentPassword ->
                view?.showError("New password must be different from the old password.")

            else -> {
                repository.updatePassword(newPassword)
                view?.clearError()
                view?.navigateToLoginAfterReset()
            }
        }
    }
}