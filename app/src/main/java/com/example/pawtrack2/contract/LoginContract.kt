package com.example.pawtrack2.contract

/**
 * Defines the passive View and the Presenter that drives it for the Login screen.
 */
interface LoginContract {

    interface View {
        fun showError(message: String)
        fun clearError()
        fun navigateToDashboard()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun onLoginClicked(email: String, password: String)
    }
}