package com.example.pawtrack2.presenter

import com.example.pawtrack2.contract.DashboardContract
import com.example.pawtrack2.model.AccountRepository

class DashboardPresenter(
    private val repository: AccountRepository
) : `DashboardContract`.Presenter {

    private var view: `DashboardContract`.View? = null

    override fun attachView(view: DashboardContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun loadDashboard() {
        val fullName = repository.getAccount()?.fullName?.takeIf { it.isNotBlank() } ?: "Pet Owner"
        view?.showWelcomeName(fullName)
    }
}