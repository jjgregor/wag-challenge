package com.jason.stackoverflowusers.activities

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.View
import com.jason.stackoverflowusers.R
import com.jason.stackoverflowusers.WagApp
import com.jason.stackoverflowusers.adapters.UsersAdapter
import com.jason.stackoverflowusers.models.UsersViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: UsersViewModel
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (application as WagApp).getAppComponent().inject(this)

        viewModel = ViewModelProviders.of(this).get(UsersViewModel::class.java)
        adapter = UsersAdapter(viewModel.users)
        swipe_refresh.setOnRefreshListener { getUsers() }

        if (viewModel.users.isEmpty()) {
            getUsers()
        } else {
            bindUsers()
        }
    }

    private fun getUsers() {
        viewModel.getUsers()?.subscribe({ response ->
            response?.let {
                viewModel.users = it.items ?: ArrayList()
                adapter.updateData(viewModel.users)
                bindUsers()
            } ?: onError()
        }, { t: Throwable? ->
            onError()
            Log.e(Companion.TAG, "Error getting users.", t)
        })

        if (swipe_refresh.isRefreshing) {
            swipe_refresh.isRefreshing = false
        }
    }

    private fun bindUsers() {
        empty_view.visibility = View.GONE
        progress_bar.visibility = View.GONE
        recycler_view.layoutManager = GridLayoutManager(this, 2)
        recycler_view.adapter = adapter
        recycler_view.visibility = View.VISIBLE
    }

    private fun onError() {
        progress_bar.visibility = View.GONE
        if (adapter.itemCount <= 0) {
            empty_view.visibility = View.VISIBLE
        }
        Snackbar.make(constraint, R.string.get_users_error, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

}
