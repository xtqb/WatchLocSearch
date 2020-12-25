package com.lhzw.watchloc

import android.annotation.SuppressLint
import android.content.Context
import android.content.LoRaManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.j256.ormlite.dao.Dao
import com.lhzw.watchloc.bean.PersonInfo
import com.lhzw.watchloc.db.CommDBOperation
import com.lhzw.watchloc.db.DatabaseHelper
import com.lhzw.watchsearch.adapter.PerInfoAdapter
import com.lhzw.watchsearch.uitls.BaseUtils
import com.lhzw.watchsearch.view.RecycleViewDivider
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    private lateinit var list: MutableList<PersonInfo>
    private lateinit var helper: DatabaseHelper
    private lateinit var perDao: Dao<PersonInfo, Int>
    private lateinit var adapter: PerInfoAdapter
    private lateinit var mUnbinder: Unbinder
    private var loRaManager: LoRaManager? = null
    private var bdByteArr: ByteArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initData()
        setListener()
    }

    private fun initView() {


    }

    @SuppressLint("WrongConstant")
    private fun initData() {
        EventBus.getDefault().register(this)
        loRaManager = getSystemService(Context.LORA_SERVICE) as LoRaManager?
        mUnbinder = ButterKnife.bind(this)
        helper = DatabaseHelper.getHelper()
        perDao = helper.getPersonDao()
        //设置线性布局管理器
        var layoutManager = LinearLayoutManager(this)
        //设置适配器
        list = CommDBOperation.queryAll(perDao, "register")
        recycler_view.layoutManager = layoutManager
        recycler_view.addItemDecoration(RecycleViewDivider(this, LinearLayoutManager.VERTICAL, 1, getColor(R.color.green)))
        adapter = PerInfoAdapter(this, list!!, true)
        recycler_view.adapter = adapter
        tv_counter.text = tv_counter.text.toString().replace("@", "0")
        adapter.setOnItemClickListner(object : PerInfoAdapter.OnItemClickListenr {
            override fun onItemClick(pos: Int) {
                sendCMDSearch(list[pos].register)
            }
        })
    }

    private fun setListener() {


    }


    @OnClick(R.id.tv_clean, R.id.tv_statistic)
    fun onClickView(view: View) {
        when (view.id) {
            R.id.tv_clean -> {
                Log.e("Tag", "clean ...")
                CommDBOperation.deleteAll(perDao)
                list.clear()
                adapter.setList(list)
                tv_counter.text = "总计：@".replace("@", if (list == null || list.size == 0) "0" else "${adapter.getMap().size}")
            }
            R.id.tv_statistic -> {
                Log.e("Tag", "statistics ...")
                if (list != null && list.size > 0) list.clear()
                list = CommDBOperation.queryAll(perDao, "register")
                adapter.setList(list)
                tv_counter.text = "总计：@".replace("@", if (list == null || list.size == 0) "0" else "${adapter.getMap().size}")
            }
        }
    }

    private fun sendCMDSearch(num: String): Boolean {
        val numByte: ByteArray? = obtainBDNum()
        val sndByte = BaseUtils.getPerRegisterByteArr(num)!!

        for (pos in 0..3) {
            sndByte[5 + pos] = numByte!![pos]
        }
        Thread { loRaManager?.searchCard(sndByte) }.start()
        return true
    }

    private fun obtainBDNum(): ByteArray? {
        if (bdByteArr == null) {
            bdByteArr = BaseUtils.obtainSearchBytes()
        }
        return bdByteArr
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun handleEvent(register: String) {
        runOnUiThread { tv_watch.text = register }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        mUnbinder?.unbind()
    }
}
