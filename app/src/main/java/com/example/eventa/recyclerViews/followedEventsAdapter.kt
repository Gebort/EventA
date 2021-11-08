package com.example.eventa.recyclerViews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.eventa.DBHelper
import com.example.eventa.Event
import com.example.eventa.R
import com.example.eventa.User
import java.text.SimpleDateFormat
import java.util.*

class followedEventsAdapter(var events: List<Event>):
        RecyclerView.Adapter<followedEventsAdapter.MyViewHolder>(){

    private var mExpandedPosition = -1
    private var previousExpandedPosition = -1

    class MyViewHolder(iv: View) : RecyclerView.ViewHolder(iv){
        var title: TextView?
        var date: TextView?
        var desc: TextView?
        var loc: TextView?
        var time: TextView?
        var number: TextView?
        var orgName: TextView?
        var orgEmail: TextView?
        var orgPhone: TextView?
        var extraLayout: ConstraintLayout?
        var unsign: Button?

        init {
            title = iv.findViewById(R.id.titleText)
            date = iv.findViewById(R.id.dateText)
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

    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        val dateStr = Date(events[i].date)
        val format = SimpleDateFormat("dd.MM.yyyy")
        h.date?.text = format.format(dateStr)
        h.desc?.text = events[i].desc
        h.loc?.text = events[i].loc
        h.number?.text = "${events[i].currPartNumber}/${events[i].partNumber}"
        h.time?.text = "${events[i].hour}:${events[i].min}"
        h.orgName?.text = "Organisator - ${events[i].orgName}"
        h.title?.text = events[i].title
        //TODO Если событие не исчезло из списка, то при апдейте списка кнопка снова станет активной
        h.unsign?.isEnabled = true
        if(events[i].showEmail){
            h.orgEmail?.text = "Email - ${events[i].orgEmail}"
            h.orgEmail?.visibility = View.VISIBLE
        }
        else{
            h.orgEmail?.text = ""
            h.orgEmail?.visibility = View.INVISIBLE
        }
        if(events[i].showPhone){
            h.orgPhone?.text = "Phone - ${events[i].orgPhone}"
            h.orgPhone?.visibility = View.VISIBLE
        }
        else{
            h.orgPhone?.text = ""
            h.orgPhone?.visibility = View.INVISIBLE
        }

        h.extraLayout?.visibility = View.GONE

        val isExpanded = i == mExpandedPosition
        h.extraLayout?.visibility = if (isExpanded) View.VISIBLE else View.GONE
        h.itemView.isActivated = isExpanded

        if (isExpanded) previousExpandedPosition = i

        h.itemView.setOnClickListener{
            mExpandedPosition = if (isExpanded) -1 else i
            notifyItemChanged(previousExpandedPosition)
            notifyItemChanged(i)
        }

        h.unsign?.setOnClickListener {
            h.unsign?.isEnabled = false
            mExpandedPosition = -1
            DBHelper.removeParticipant(events[i].id!!, events[i].city!!, User.email, ::onRemoveResult)
        }

    }

    override fun getItemCount(): Int {
        return events.size
    }

    fun onRemoveResult(result: Boolean){

    }

}