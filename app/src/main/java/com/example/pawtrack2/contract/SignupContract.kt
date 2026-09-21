package com.example.pawtrack2.contract

interface SignUpContract {

    interface View {
        fun showError(message: String)
        fun clearError()
        fun navigateToLoginAfterSignUp()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun onSignUpClicked(
            fullName: String,
            email: String,
            password: String,
            confirmPassword: String
        )
    }
}