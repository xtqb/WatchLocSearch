package com.lhzw.watchsearch.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.lhzw.watchloc.R
import com.lhzw.watchloc.bean.PersonInfo

/**
 *
@author：created by xtqb
@description:
@date : 2019/10/17 8:49
 *
 */
class PerInfoAdapter(private var mContext: Context, private var list: MutableList<PersonInfo>, private var state: Boolean) : RecyclerView.Adapter<PerInfoAdapter.ViewHolder>() {
    private lateinit var holder: ViewHolder
    private var listner: OnItemClickListenr? = null
    private var map: MutableMap<String, Int> = HashMap()
    private var itemList: MutableList<Item> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(mContext).inflate(R.layout.item_watch_person, null)
        holder = ViewHolder(view)
        holder.itemView.setOnClickListener(ItemOnClick())
        holder.tv_bind.setOnClickListener(ItemOnClick())
        holder.tv_tv_send_cmd.setOnClickListener(ItemOnClick())
        return holder
    }

    init {
        initData()
    }

    private fun initData() {
        map.clear()
        itemList.clear()
        list?.forEach {
            var tmp = map.get(it.register)
            if (tmp == null) {
                map.put(it.register, 1)
            } else {
                tmp += 1
                map.put(it.register, tmp!!)
            }
        }

        map?.forEach {
            var item = Item(it.key, it.value)
            itemList.add(item)
        }
    }


    override fun getItemCount(): Int {
        if (map == null) {
            return 0
        }
        return map.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.im_portrait?.setBackgroundResource(R.drawable.icon_common)
        holder?.tv_name?.text = itemList.get(position).register
        holder?.tv_content?.text = "接收次数 \t\t " + itemList.get(position).num
        holder?.itemView?.tag = position
        holder?.tv_bind?.tag = position
        holder?.tv_tv_send_cmd?.tag = position
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var im_portrait: ImageView = view.findViewById(R.id.im_portrait) as ImageView
        var tv_name: TextView = view.findViewById(R.id.tv_name) as TextView
        var tv_content: TextView = view.findViewById(R.id.tv_content) as TextView
        var tv_bind: TextView = view.findViewById(R.id.tv_bind) as TextView
        var tv_tv_send_cmd: TextView = view.findViewById(R.id.tv_send_cmd) as TextView
    }

    private inner class ItemOnClick : View.OnClickListener {
        override fun onClick(v: View?) {
            var pos: Int = v!!.tag as Int
            listner?.onItemClick(v,pos)
        }

    }

    fun setList(list: MutableList<PersonInfo>) {
        this.list = list
        initData()
        notifyDataSetChanged()
    }

    fun getMap() = map

    interface OnItemClickListenr {
        fun onItemClick(v:View?,pos: Int)
    }

    fun setOnItemClickListner(callback: OnItemClickListenr) {
        this.listner = callback
    }

    private inner class Item constructor(var register: String, var num: Int)
}