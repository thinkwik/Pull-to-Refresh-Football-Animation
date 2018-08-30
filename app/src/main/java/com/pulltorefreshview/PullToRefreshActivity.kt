package com.pulltorefreshview

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.thinkwik.pulltorefresh.FootballPullToRefreshView
import com.pulltorefreshview.adapter.GameListAdapter
import kotlinx.android.synthetic.main.activity_pull_to_refresh.*

class PullToRefreshActivity : AppCompatActivity() {
    private val refreshDelay = 3000
    private lateinit var mContext: Context
    private lateinit var mAdapter: GameListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        mContext = this@PullToRefreshActivity
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pull_to_refresh)
        init()
    }

    private fun init() {
        recyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)

        mAdapter = GameListAdapter()
        recyclerView.adapter = mAdapter

        mPullToRefreshView.setOnRefreshListener(object : FootballPullToRefreshView.OnRefreshListener {
            override fun onRefresh() {
                mPullToRefreshView.postDelayed({ mPullToRefreshView.setRefreshing(false) }, refreshDelay.toLong())
            }
        })
    }
}
