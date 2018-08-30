package com.pulltorefreshview.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pulltorefreshview.R
import com.pulltorefreshview.entity.Games
import kotlinx.android.synthetic.main.raw_hub_feeds.view.*

class GameListAdapter : RecyclerView.Adapter<GameListAdapter.ListHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.raw_hub_feeds, parent, false)
        return ListHolder(view)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: ListHolder, position: Int) {
        holder.onBind()
    }

    inner class ListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind() {

        }
    }
}