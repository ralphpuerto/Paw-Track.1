package com.example.pawtrack2.contract

interface ForgotPasswordContract {

    interface View {
        fun showError(message: String)
        fun clearError()
        fun advanceToPasswordStep(verifiedEmail: String)
        fun navigateToLoginAfterReset()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun onVerifyEmailClicked(email: String)
        fun onResetPasswordClicked(newPassword: String, confirmPassword: String)
    }
}