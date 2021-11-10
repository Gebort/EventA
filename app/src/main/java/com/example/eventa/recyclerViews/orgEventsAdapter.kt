package com.example.eventa.recyclerViews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.eventa.Event
import com.example.eventa.R
import java.text.SimpleDateFormat
import java.util.*

class orgEventsAdapter(var events: MutableList<Event>):
        RecyclerView.Adapter<orgEventsAdapter.MyViewHolder>(){

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
        var edit: Button?
        var delete: Button?

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
            edit = iv.findViewById(R.id.editBut)
            delete = iv.findViewById(R.id.deleteBut)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_organised_event, parent, false)
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

        if(events[i].showEmail){
            h.orgEmail?.text = "Email - ${events[i].orgEmail}"
            h.orgEmail?.visibility = View.VISIBLE
        }
        else{
            h.orgEmail?.text = ""
            h.orgEmail?.visibility = View.INVISIBLE
        }
        if(events[i].showNumber){
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

        //TODO возможность просмотреть список участников и информацию о них

        h.edit?.setOnClickListener {
            h.edit?.isEnabled = false
            //TODO возможность менять события
        }

        h.delete?.setOnClickListener {
            h.delete?.isEnabled = false
            //TODO возможность удалять события
        }

    }

    override fun getItemCount(): Int {
        return events.size
    }

    fun onAddResult(result: Boolean){

    }

}