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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.eventa.DBHelper
import com.example.eventa.Event
import com.example.eventa.R
import com.example.eventa.mainFragments.MyEventsDirections
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


class orgEventsAdapter(var events: MutableList<Event>, val rView: RecyclerView):
        RecyclerView.Adapter<orgEventsAdapter.MyViewHolder>(){

    var noSelected = -1
    var mExpandedPosition = noSelected
    var previousExpandedPosition = noSelected

    class MyViewHolder(iv: View) : RecyclerView.ViewHolder(iv){
        var title: TextView?
        var date: TextView?
        var desc: TextView?
        var requestsText: TextView?
        var requestsButton: Button?
        var requestsImage: ImageView?
        var loc: TextView?
        var lock: ImageView?
        var time: TextView?
        var number: TextView?
        var orgName: TextView?
        var orgEmail: TextView?
        var orgPhone: TextView?
        var extraLayout: ConstraintLayout?
        var edit: Button?
        var delete: Button?
        var partBut: Button?

        init {
            title = iv.findViewById(R.id.titleText)
            date = iv.findViewById(R.id.dateText)
            desc = iv.findViewById(R.id.descText)
            lock = iv.findViewById(R.id.lockImage)
            loc = iv.findViewById(R.id.locText)
            requestsButton = iv.findViewById(R.id.requestsButton)
            requestsText = iv.findViewById(R.id.requestsText)
            requestsImage = iv.findViewById(R.id.requestsImage)
            time = iv.findViewById(R.id.timeText)
            number = iv.findViewById(R.id.numberText)
            orgName = iv.findViewById(R.id.orgNameText)
            orgEmail = iv.findViewById(R.id.orgEmailText)
            orgPhone = iv.findViewById(R.id.orgPhoneText)
            extraLayout = iv.findViewById(R.id.layoutExtra)
            edit = iv.findViewById(R.id.editBut)
            delete = iv.findViewById(R.id.deleteBut)
            partBut = iv.findViewById(R.id.partBut)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_organised_event, parent, false)
        return MyViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(h: MyViewHolder, i: Int) {
        val instant = Instant.ofEpochMilli(events[i]!!.date)
        var dateSnap = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

        val dateStr = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(dateSnap)
        val timeStr = DateTimeFormatter.ofPattern("HH.mm").format(dateSnap)
        h.desc?.text = events[i].desc
        h.lock?.isVisible = !events[i].public
        if (events[i]!!.city != null){
            h.loc?.text = "${events[i]!!.city}, ${events[i]!!.loc}"
        }
        else{
            h.loc?.text = rView.resources.getString(R.string.no_place)
        }
        h.number?.text = "${events[i].currPartNumber}/${events[i].partNumber}"
        h.time?.text = timeStr
        if (events[i]?.today){
            h.date?.text = rView.context.resources.getString(R.string.today)
        }
        else{
            h.date?.text = dateStr
        }
        if (!events[i].public){
            val requests = events[i].requests ?: listOf()
            h.requestsText?.visibility = View.VISIBLE
            h.requestsButton?.visibility = View.VISIBLE
            h.requestsImage?.visibility = View.VISIBLE
            h.requestsText?.setText(requests.size.toString())
            h.requestsButton?.isEnabled = requests.isNotEmpty()
        }
        else{
            h.requestsText?.visibility = View.GONE
            h.requestsButton?.visibility = View.GONE
            h.requestsImage?.visibility = View.GONE
        }
        h.orgName?.text = String.format(rView.context.resources.getString(R.string.organisator), events[i].orgName)
        h.title?.text = events[i].title
        h.delete?.isEnabled = true
        h.edit?.isEnabled = true
        h.partBut?.isEnabled = false
        if (events[i].users != null){
            if (events[i].users!!.isNotEmpty()){
                h.partBut?.isEnabled = true
            }
        }

        if(events[i].showEmail){
            h.orgEmail?.text = String.format(rView.context.resources.getString(R.string.organisator_email), events[i].orgEmail)
            h.orgEmail?.visibility = View.VISIBLE
        }
        else{
            h.orgEmail?.text = ""
            h.orgEmail?.visibility = View.INVISIBLE
        }
        if(events[i].showNumber){
            h.orgPhone?.text = String.format(rView.context.resources.getString(R.string.organisator_phone), events[i].orgPhone)
            h.orgPhone?.visibility = View.VISIBLE
        }
        else{
            h.orgPhone?.text = ""
            h.orgPhone?.visibility = View.INVISIBLE
        }

        h.extraLayout?.visibility = View.GONE

        val isExpanded = h.layoutPosition == mExpandedPosition
        h.extraLayout?.visibility = if (isExpanded) View.VISIBLE else View.GONE
        h.itemView.isActivated = isExpanded

        if (isExpanded) previousExpandedPosition = mExpandedPosition

        h.itemView.setOnClickListener{
            mExpandedPosition = if (isExpanded) noSelected else h.layoutPosition
            notifyItemChanged(previousExpandedPosition)
            notifyItemChanged(mExpandedPosition)
        }

        h.partBut?.setOnClickListener {
            if (events[i].users != null) {
                val items = events[i].users?.toTypedArray()
                MaterialAlertDialogBuilder(rView.context)
                        .setTitle(R.string.participants_list)
                        .setItems(items) { _, which ->
                            MaterialAlertDialogBuilder(rView.context)
                                    .setMessage(String.format(rView.context.resources.getString(R.string.kick_participant), items?.get(which)))
                                    .setNegativeButton(R.string.no){ dialog, _ ->
                                        dialog.cancel()
                                    }
                                    .setPositiveButton(R.string.yes){ _, _ ->
                                        DBHelper.removeParticipant(events[i].id.toString(), items?.get(which)!!){ result ->
                                            onRemoveResult(result)
                                        }
                                    }
                                    .show()
                        }
                        .show()
            }
        }

        h.requestsButton?.setOnClickListener {
            if (events[i].requests != null) {
                val items = events[i].requests?.toTypedArray()
                MaterialAlertDialogBuilder(rView.context)
                        .setTitle(R.string.requests_list)
                        .setItems(items) { _, which ->
                            MaterialAlertDialogBuilder(rView.context)
                                    .setMessage(String.format(rView.context.resources.getString(R.string.add_participant), items?.get(which)))
                                    .setNegativeButton(R.string.no){ dialog, _ ->
                                        dialog.cancel()
                                    }
                                    .setPositiveButton(R.string.yes){ _, _ ->
                                        DBHelper.removeRequestAddParticipant(events[i].id.toString(), items?.get(which)!!){ result ->
                                            onAddResult(result)
                                        }
                                    }
                                    .show()
                        }
                        .show()
            }
        }

        h.edit?.setOnClickListener {
            h.edit?.isEnabled = false
            val action = MyEventsDirections.actionMyEventsToOrgEvents()
            action.edit = true
            action.eventIndex = i
            rView.findNavController().navigate(action)
            h.edit?.isEnabled = true
            mExpandedPosition = noSelected
            notifyItemChanged(previousExpandedPosition)
            notifyItemChanged(mExpandedPosition)
        }

        h.delete?.setOnClickListener {
            h.delete?.isEnabled = false


            MaterialAlertDialogBuilder(rView.context)
                .setMessage(String.format(rView.context.resources.getString(R.string.event_delete_confirm), events[i].title))
                .setNegativeButton(R.string.no){ dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton(R.string.yes){ _, _ ->
                    DBHelper.deleteEvent(events[i].id.toString()){ result ->
                        onDeleteResult(result)
                    }
                }
                .setOnCancelListener{
                    h.delete?.isEnabled = true
                }
                .show()
        }

    }

    override fun getItemCount(): Int {
        return events.size
    }

    private fun onDeleteResult(result: Boolean){
        if (result) {
            Snackbar.make(rView, R.string.event_deleted, Snackbar.LENGTH_SHORT).setAnchorView(rView)
                .show()
        }
        else{
            Snackbar.make(rView, R.string.event_not_deleted, Snackbar.LENGTH_SHORT).setAnchorView(rView)
                .show()
        }
    }

    private fun onRemoveResult(result: Boolean){
        if (result) {
            Snackbar.make(rView, R.string.participant_kicked, Snackbar.LENGTH_SHORT).setAnchorView(rView)
                    .show()
        }
        else{
            Snackbar.make(rView, R.string.failed_to_kick_participant, Snackbar.LENGTH_SHORT).setAnchorView(rView)
                    .show()
        }
    }

    private fun onAddResult(result: Boolean){
        if (result) {
            Snackbar.make(rView, R.string.participant_added, Snackbar.LENGTH_SHORT).setAnchorView(rView)
                    .show()
        }
        else{
            Snackbar.make(rView, R.string.failed_to_add_participant, Snackbar.LENGTH_SHORT).setAnchorView(rView)
                    .show()
        }
    }

}