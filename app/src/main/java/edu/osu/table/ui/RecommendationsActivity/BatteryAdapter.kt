package edu.osu.table.ui.RecommendationsActivity

import android.content.Context
import android.support.constraint.R.id.parent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.osu.table.R
import kotlinx.android.synthetic.main.recommendation_item.view.*

class BatteryAdapter (val date_list: List<String>, val bat_delta: List<Float>,
                     val context: Context) : RecyclerView.Adapter<ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recommendation_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.recommendationtype?.text = date_list.get(position)
    }

    override fun getItemCount(): Int {
        return date_list.size
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val recommendationtype = view.recommendation_textview
}
