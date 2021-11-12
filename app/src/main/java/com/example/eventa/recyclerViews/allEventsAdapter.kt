package com.example.eventa.recyclerViews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventa.DBHelper
import com.example.eventa.Event
import com.example.eventa.R
import com.example.eventa.User
import java.text.SimpleDateFormat
import java.util.*


class allEventsAdapter(val rView: RecyclerView, var visibleThreshold: Int, var events: MutableList<Event?>, callbackLoadMore: (() -> Unit)?):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1
    private var mExpandedPosition = -1
    private var previousExpandedPosition = -1
    private var totalItemCount = 0
    private var lastVisibleItem = 0
    var isLoading = false

    init{
        val linearLayoutManager = rView.layoutManager as LinearLayoutManager
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (events.size > 0) {
                    totalItemCount = linearLayoutManager?.itemCount
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                    if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                        if (callbackLoadMore != null) {
                            callbackLoadMore()
                        }
                    }
                }
            }
        }
        rView.addOnScrollListener(scrollListener)
    }

     class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var progressBar: ProgressBar

        init {
            progressBar = view.findViewById<View>(R.id.progressBar1) as ProgressBar
        }
    }

    class MyViewHolder(iv: View) : RecyclerView.ViewHolder(iv){
        var title: TextView?
        var date: TextView?
        var desc: TextView?
        var loc: TextView?
        var time: TextView?
        var number: TextView?
        var extraLayout: ConstraintLayout?
        var signup: Button?

        init {
            title = iv.findViewById(R.id.titleText)
            date = iv.findViewById(R.id.dateText)
            desc = iv.findViewById(R.id.descText)
            loc = iv.findViewById(R.id.locText)
            time = iv.findViewById(R.id.timeText)
            number = iv.findViewById(R.id.numberText)
            extraLayout = iv.findViewById(R.id.layoutExtra)
            signup = iv.findViewById(R.id.signupBut)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_ITEM) {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
            return MyViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            return LoadingViewHolder(view)
        }
    }

    override  fun onBindViewHolder(h: RecyclerView.ViewHolder, i: Int) {
        if (h is MyViewHolder) {
            if (events[i] != null) {
                val dateStr = Date(events[i]!!.date)
                val format = SimpleDateFormat("dd.MM.yyyy")
                h.date?.text = format.format(dateStr)
                h.desc?.text = events[i]!!.desc
                h.loc?.text = events[i]!!.loc
                h.number?.text = "${events[i]!!.currPartNumber}/${events[i]!!.partNumber}"
                h.time?.text = "${events[i]!!.hour}:${events[i]!!.min}"
                h.title?.text = events[i]!!.title
                h.extraLayout?.visibility = View.GONE
                h.signup?.isEnabled = true

                val isExpanded = h.layoutPosition == mExpandedPosition
                h.extraLayout?.visibility = if (isExpanded) View.VISIBLE else View.GONE
                h.itemView.isActivated = isExpanded

                if (isExpanded) previousExpandedPosition = mExpandedPosition

                h.itemView.setOnClickListener {
                    mExpandedPosition = if (isExpanded) -1 else h.layoutPosition
                    notifyItemChanged(previousExpandedPosition)
                    notifyItemChanged(mExpandedPosition)
                }

                h.signup?.setOnClickListener {
                    h.signup?.isEnabled = false
                    previousExpandedPosition = -1
                    mExpandedPosition = -1
                    DBHelper.addParticipant(events[i]!!.id!!, events[i]!!.city!!, User.email, ::onAddResult)
                }
            }
        }
        else if (h is LoadingViewHolder){
            h.progressBar.isIndeterminate = true
        }

    }


    fun setLoaded(){
        isLoading = false
    }

    override fun getItemViewType(position: Int): Int {
        return if (events.get(position) == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return events.size
    }

    fun onAddResult(result: Boolean){

    }

}