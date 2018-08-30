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
    private var gameList = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        mContext = this@PullToRefreshActivity
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pull_to_refresh)
        init()
    }

    private fun init() {
        recyclerView.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)

        dummyGames()

        mAdapter = GameListAdapter(gameList)
        recyclerView.adapter = mAdapter

        mPullToRefreshView.setOnRefreshListener(object : FootballPullToRefreshView.OnRefreshListener {
            override fun onRefresh() {
                mPullToRefreshView.postDelayed({ mPullToRefreshView.setRefreshing(false) }, refreshDelay.toLong())
            }
        })
    }

    private fun dummyGames() {
        gameList.add(R.drawable.ic_dummy1)
        gameList.add(R.drawable.ic_dummy2)
        gameList.add(R.drawable.ic_dummy3)
        gameList.add(R.drawable.ic_dummy4)
        gameList.add(R.drawable.ic_dummy5)
        gameList.add(R.drawable.ic_dummy6)
        gameList.add(R.drawable.ic_dummy7)
        gameList.add(R.drawable.ic_dummy8)
    }
}
