package com.example.pawtrack2.contract

interface `DashboardContract` {

    interface View {
        fun showWelcomeName(fullName: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadDashboard()
    }
}