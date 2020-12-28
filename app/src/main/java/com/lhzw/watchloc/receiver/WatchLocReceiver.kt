package com.lhzw.watchloc.receiver

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ProtocolParser
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import com.lhzw.watchloc.R
import com.lhzw.watchloc.bean.PersonInfo
import com.lhzw.watchloc.db.CommDBOperation
import com.lhzw.watchloc.db.DatabaseHelper
import com.lhzw.watchsearch.constants.Constants
import com.lhzw.watchsearch.uitls.BaseUtils
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat

/**
 *
@author：created by xtqb
@description:
@date : 2019/10/24 13:56
 *
 */

class WatchLocReceiver : BroadcastReceiver() {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("WatchLocReceiver", "receive 433 info ...  ")
        try {
            var parser: ProtocolParser? = intent?.getParcelableExtra("result")
            if (parser == null) return
            var type = parser.cmdKey //指令号

            val lng = BaseUtils.ByteToStringForLocInfo(parser.getLongitude())//经度
            val lat = BaseUtils.ByteToStringForLocInfo(parser.getLatitude())//纬度
            val register_num = BaseUtils.traslation(parser.getPersonNum()!!).substring(0, 10)//注册码
            Log.e("WatchLocReceiver", "receive  type = ${type[0]} ,register_num=${register_num} ")
            val locTime = BaseUtils.byteArrToTime(parser.timeStamp)//指令时间
            var datatype = "SOS"//数据类型
            var icon = R.drawable.icon_sos2//sos图标
            var perType = Constants.PER_SOS//sos人员类型
            when (type!![0]) {//指令号
                0x11.toByte(), 0xA3.toByte() -> {//普通指令
                    icon = R.drawable.icon_common
                    perType = Constants.PER_COMMON
                }
                0x12.toByte() -> {
                    Log.d("WA","收到腕表绑定回复mac =$register_num,当前绑定需求为${BaseUtils.getString("WatchMac","")}")
                    if(register_num == BaseUtils.getString("WatchMac","")){
                        Toast.makeText(context,"${register_num}绑定成功",Toast.LENGTH_LONG).show()
                        BaseUtils.putString("WatchMac","")
                    }else{
                        Toast.makeText(context,"收到${register_num}SOS请求",Toast.LENGTH_SHORT).show()
                    }
                }
                0xA1.toByte()->{

                }
            }
            var item = PersonInfo(perType, register_num, "", 0, "", lat, lng, sdf.format(locTime))//新建腕表bean
            var dao = DatabaseHelper.getHelper().getPersonDao()
            CommDBOperation.saveToDB(dao, item)//将收到的手表人员入库
            showNotification(context!!, register_num, datatype, icon, "")//同步到通知栏
            EventBus.getDefault().post(register_num)//发送收到的腕表注册码
        } catch (e: Exception) {

        }
        if (!BaseUtils.getBoolean(Constants.SP_SWITCH_SIGNAL, true)) {
            return
        }

    }


    companion object {
        @JvmStatic
        private fun showNotification(mContext: Context, text: String, type: String, id: Int, subText: String) {
            var n = Notification.Builder(mContext).setContentTitle(mContext.getString(R.string._noe_receiver_signal).replace("@", type)).setContentText(text)
                    .setSubText(subText).setSmallIcon(id).setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), id))
                    .setWhen(System.currentTimeMillis())
                    .build();

            n.flags = Notification.FLAG_AUTO_CANCEL;
            // n.number=count;
            n.defaults = Notification.DEFAULT_SOUND;
            // 从系统服务中获得通知管理器

            var nm: NotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            // 执行通知
            nm.notify(1, n);
        }
    }


}