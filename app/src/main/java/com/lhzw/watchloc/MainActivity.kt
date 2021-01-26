package com.lhzw.watchloc

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.LoRaManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.j256.ormlite.dao.Dao
import com.lhzw.watchloc.adapter.CommandAdapter
import com.lhzw.watchloc.adapter.SelectChannelAdapter
import com.lhzw.watchloc.bean.PersonInfo
import com.lhzw.watchloc.db.CommDBOperation
import com.lhzw.watchloc.db.DatabaseHelper
import com.lhzw.watchloc.dialog.SendMessageDialog
import com.lhzw.watchloc.dialog.ShowAlertDialogCommand
import com.lhzw.watchloc.dialog.ShowSelectChanelDialog
import com.lhzw.watchsearch.adapter.PerInfoAdapter
import com.lhzw.watchsearch.constants.Constants
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

    private val dialog by lazy {
        ShowSelectChanelDialog(this)
    }
    private var chanelNum = 0
    private fun initView() {
        chanelNum = BaseUtils.getInt(Constants.CHANNEL_NUM, 0)
        loRaManager?.changeWatchType(chanelNum)//改变手持底层信道
        tv_chanel_num.text = "信道号:$chanelNum"
        tv_chanel_num.setOnClickListener {
            dialog.show()
            dialog.setAdapter(SelectChannelAdapter(this, resources.getStringArray(R.array.signal_channel))) {
                dialog.dismiss()
            }
            dialog.setOnSelecteChannelClickListener { pos ->
                chanelNum = pos
                BaseUtils.putInt(Constants.CHANNEL_NUM, chanelNum)
                tv_chanel_num.text = "信道号:$chanelNum"
                loRaManager?.changeWatchType(chanelNum)//改变手持底层信道
                dialog.dismiss()
            }
        }



    }

    @SuppressLint("WrongConstant")
    private fun initData() {
        EventBus.getDefault().register(this)
        loRaManager = getSystemService(Context.LORA_SERVICE) as LoRaManager?
        mUnbinder = ButterKnife.bind(this)
        helper = DatabaseHelper.getHelper()
        perDao = helper.getPersonDao()
        //设置线性布局管理器
        val layoutManager = LinearLayoutManager(this)
        //设置适配器
        list = CommDBOperation.queryAll(perDao, "register")//腕表列表

        recycler_view.layoutManager = layoutManager
        recycler_view.addItemDecoration(RecycleViewDivider(this, LinearLayoutManager.VERTICAL, 1, getColor(R.color.green)))
        adapter = PerInfoAdapter(this, list, true)
        recycler_view.adapter = adapter

        tv_counter.text = tv_counter.text.toString().replace("@", "0")

        adapter.setOnItemClickListner(object : PerInfoAdapter.OnItemClickListenr {
            override fun onItemClick(v: View?, pos: Int) {
                if (v?.id == R.id.tv_content) {//单搜
                    Toast.makeText(this@MainActivity, "单人搜索成功", Toast.LENGTH_SHORT).show()
                    sendCMDSearch(list[pos].register)
                } else if (v?.id == R.id.tv_bind) {//绑定
                    // 1.将当前手持切换到  0信道 延迟500ms   然后发送绑定指令成功后再延迟500ms切到当前信道
                    v.isEnabled = false
                    loRaManager?.changeWatchType(0)
                    Handler().postDelayed(Runnable {
                            sendCMDBind(list[pos].register)
                            Toast.makeText(this@MainActivity, "发送绑定成功", Toast.LENGTH_SHORT).show()
                            Handler().postDelayed({
                                    //切换到当前信道
                                    loRaManager?.changeWatchType(chanelNum)
                                    v.isEnabled = true//可用

                            }, 5000)


                    }, 2000)

                } else if (v?.id == R.id.tv_send_cmd) {//单发
                    showAlertDialogCommand(BaseUtils.getPerRegisterByteArr(list[pos].register))
                }


            }
        })
    }

    private fun setListener() {
        tv_cp_info.setOnClickListener {
            Intent(this,CpInfoActivity::class.java).apply {
                startActivity(this)
            }
        }
        tv_search.setOnClickListener {
            //发送搜索指令
            sendCMDAllSearch()
            Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                runOnUiThread {
                    //刷新列表
                    refrshList()

                }
            }, 5000)
        }

        //群发指令
        tv_send_all.setOnClickListener {
            val bytes = ByteArray(11)
            showAlertDialogCommand(bytes)
        }


    }

    private var dialogCommmand: ShowAlertDialogCommand? = null


    //发送指令
    private fun showAlertDialogCommand(sndBytes: ByteArray) {
        dialogCommmand = ShowAlertDialogCommand(this)
        dialogCommmand?.sendBytes = sndBytes
        dialogCommmand?.showDialog()
        dialogCommmand?.setListener { view ->
            when (view.id) {
                R.id.tv_command_cancel -> {//取消
                    dialogCommmand?.cancel()
                }
                R.id.tv_send_message -> {//自定义指令
                    dialogCommmand?.let {
                        it.dismiss()
                        val snd = it.sendBytes
                        val numByte1 = obtainBDNum()
                        for (i in 0..3) {
                            snd[5 + i] = numByte1!![i]
                        }
                        val msgDialog = SendMessageDialog(this, loRaManager, snd)
                        msgDialog.showDialog()
                        msgDialog.setListener()
                    }

                }
            }
        }
        dialogCommmand?.setAdapter(CommandAdapter(this))
        dialogCommmand?.setOnItemClickListener { parent, view, position, id ->
            val numByte1 = obtainBDNum()
            for (i in 0..3) {
                sndBytes[5 + i] = numByte1!![i]
            }
            val pos = position + 1
            sendCMDAction(sndBytes, (pos and 0xFF).toByte())
            dialogCommmand?.clear()
        }

    }

    private fun sendCMDAction(sndBytes: ByteArray, cmd: Byte) {
        Thread {
            loRaManager?.sendActionCmd(sndBytes, cmd)
        }.start()
        Toast.makeText(this, "发送指令成功", Toast.LENGTH_SHORT).show()
    }


    @OnClick(R.id.tv_clean, R.id.tv_statistic)
    fun onClickView(view: View) {
        when (view.id) {
            R.id.tv_clean -> {
                Log.e("Tag", "clean ...")
                CommDBOperation.deleteAll(perDao)
                list.clear()
                adapter.setList(list)
                tv_counter.text = "总计：@".replace("@", if (list.size == 0) "0" else "${adapter.getMap().size}")
            }
            R.id.tv_statistic -> {
                Log.e("Tag", "statistics ...")
                refrshList()
            }
        }
    }

    private fun refrshList() {
        if (list.size > 0) list.clear()
        list = CommDBOperation.queryAll(perDao, "register")
        adapter.setList(list)
        tv_counter.text = "总计：@".replace("@", if (list.size == 0) "0" else "${adapter.getMap().size}")
    }

    //发绑定指令 运行在子线程
    private fun sendCMDBind(num: String) {
        BaseUtils.putString("WatchMac", num)//用于接收校验
        val sndBytes = BaseUtils.getPerRegisterByteArr(num)//手表mac 前5位
        val numByte = obtainBDNum()//获取byte[11]  前5个字节为手持mac后8位+2位频道号组成的str 转为byte[]的前5位  后6位为0
        for (pos in 0..4) {
            sndBytes[5 + pos] = numByte!![pos]
        }
        sndBytes[10] = 2
        Log.e("send", " sndBytes[9]=${sndBytes[9]}")
        loRaManager?.searchCard(sndBytes)


    }


    //发送搜索指令
    private fun sendCMDSearch(num: String): Boolean {
        val numByte: ByteArray? = obtainBDNum()//获取byte[11]  前5个字节为手持mac后8位+2位频道号组成的str 转为byte[]的前5位  后6位为0
        val sndByte = BaseUtils.getPerRegisterByteArr(num)//获取byte[11]  手表注册码str 转为byte[]的前5位

        for (pos in 0..4) {
            sndByte[5 + pos] = numByte!![pos]     //sendbyte[]   6 - 10 位赋值为手持mac信息
        }
        Thread { loRaManager?.searchCard(sndByte) }.start()
        return true
    }

    //发送搜索指令
    private fun sendCMDAllSearch(): Boolean {
        val CHANNEL: Int = BaseUtils.getInt(Constants.CHANNEL_NUM, 0)//获取频道号  默认为0
        Log.e("chanel", "CHANNEL=$CHANNEL")
        val byteArr = ByteArray(11)
        byteArr[9] = CHANNEL.toByte()
        Thread { loRaManager?.searchCard(byteArr) }.start()
        return true
    }

    //获取绑定mac
    private fun obtainBDNum(): ByteArray? {
        bdByteArr = BaseUtils.obtainSearchBytes()
        return bdByteArr
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(register: String) {
      tv_watch.text = register
        //刷新列表
        refrshList()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        mUnbinder.unbind()
    }
}
