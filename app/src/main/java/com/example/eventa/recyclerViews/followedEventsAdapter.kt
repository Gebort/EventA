package com.example.eventa.recyclerViews

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.eventa.DBHelper
import com.example.eventa.Event
import com.example.eventa.R
import com.example.eventa.User
import java.time.*
import java.time.format.DateTimeFormatter

class followedEventsAdapter(val rView: RecyclerView, var events: List<Event>, val expanded: (Int) -> Unit):
        RecyclerView.Adapter<followedEventsAdapter.MyViewHolder>(){

    private var mExpandedPosition = -1
    private var previousExpandedPosition = -1

    class MyViewHolder(iv: View) : RecyclerView.ViewHolder(iv){
        var notificationImage: ImageView?
        var title: TextView?
        var date: TextView?
        var desc: TextView?
        var lock: ImageView?
        var loc: TextView?
        var time: TextView?
        var number: TextView?
        var orgName: TextView?
        var orgEmail: TextView?
        var orgPhone: TextView?
        var extraLayout: ConstraintLayout?
        var unsign: Button?

        init {
            notificationImage = iv.findViewById(R.id.notificationImage)
            title = iv.findViewById(R.id.titleText)
            date = iv.findViewById(R.id.dateText)
            lock = iv.findViewById(R.id.lockImage)
            desc = iv.findViewById(R.id.descText)
            loc = iv.findViewById(R.id.locText)
            time = iv.findViewById(R.id.timeText)
            number = iv.findViewById(R.id.numberText)
            orgName = iv.findViewById(R.id.orgNameText)
            orgEmail = iv.findViewById(R.id.orgEmailText)
            orgPhone = iv.findViewById(R.id.orgPhoneText)
            extraLayout = iv.findViewById(R.id.layoutExtra)
            unsign = iv.findViewById(R.id.unsignBut)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_followed_event, parent, false)
        return MyViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        h.notificationImage?.isVisible = events[i].notification

        val instant = Instant.ofEpochMilli(events[i]!!.date)
        var dateSnap = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

        val dateStr = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(dateSnap)
        val timeStr = DateTimeFormatter.ofPattern("HH.mm").format(dateSnap)
        h.desc?.text = events[i].desc
        h.lock?.isVisible = !events[i].public
        if (events[i]?.today){
            h.date?.text = rView.context.resources.getString(R.string.today)
        }
        else{
            h.date?.text = dateStr
        }
        if (events[i]!!.city != null){
            h.loc?.text = "${events[i]!!.city}, ${events[i]!!.loc}"
        }
        else{
            h.loc?.text = rView.resources.getString(R.string.no_place)
        }
        h.number?.text = "${events[i].currPartNumber}/${events[i].partNumber}"
        h.time?.text = timeStr
        h.orgName?.text = String.format(rView.context.resources.getString(R.string.organisator), events[i].orgName)
        h.title?.text = events[i].title
        h.unsign?.isEnabled = true
        if (events[i].showEmail){
            h.orgEmail?.text = String.format(rView.context.resources.getString(R.string.organisator_email), events[i].orgEmail)
            h.orgEmail?.visibility = View.VISIBLE
        }
        else{
            h.orgEmail?.text = ""
            h.orgEmail?.visibility = View.GONE
        }
        if (events[i].showNumber){
            h.orgPhone?.text = String.format(rView.context.resources.getString(R.string.organisator_phone), events[i].orgPhone)
            h.orgPhone?.visibility = View.VISIBLE
        }
        else{
            h.orgPhone?.text = ""
            h.orgPhone?.visibility = View.GONE
        }

        h.extraLayout?.visibility = View.GONE

        val isExpanded = h.layoutPosition == mExpandedPosition
        h.extraLayout?.visibility = if (isExpanded) View.VISIBLE else View.GONE
        h.itemView.isActivated = isExpanded

        if (isExpanded) previousExpandedPosition = mExpandedPosition

        h.itemView.setOnClickListener{
            expanded(h.layoutPosition)
            mExpandedPosition = if (isExpanded) -1 else h.layoutPosition
            notifyItemChanged(previousExpandedPosition)
            notifyItemChanged(mExpandedPosition)

        }

        h.unsign?.setOnClickListener {
            h.unsign?.isEnabled = false
            mExpandedPosition = -1
            previousExpandedPosition = -1
            DBHelper.removeParticipant(events[i].id!!, User.email){ result ->
                onRemoveResult(result)
            }
        }

    }

    override fun getItemCount(): Int {
        return events.size
    }

    fun onRemoveResult(result: Boolean){

    }

}