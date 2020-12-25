package com.lhzw.watchloc.receiver

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ProtocolParser
import android.graphics.BitmapFactory
import android.util.Log
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
        Log.e("Tag", "receive 433 info ...  ")
        try {
            var parser: ProtocolParser? = intent?.getParcelableExtra("result")
            if (parser == null) return
            var type = parser.cmdKey
            val lng = BaseUtils.ByteToStringForLocInfo(parser.getLongitude())
            val lat = BaseUtils.ByteToStringForLocInfo(parser.getLatitude())
            val register_num = BaseUtils.traslation(parser.getPersonNum()!!).substring(0, 10)
            val locTime = BaseUtils.byteArrToTime(parser.timeStamp)
            var datatype = "SOS"
            var icon = R.drawable.icon_sos2
            var perType = Constants.PER_SOS
            when (type!![0]) {
                0x11.toByte(), 0xA3.toByte() -> {
                    icon = R.drawable.icon_common
                    perType = Constants.PER_COMMON
                }
                0xA1.toByte(), 0x12.toByte() -> {
                }
            }
            var item = PersonInfo(perType, register_num, "", 0, "", lat, lng, sdf.format(locTime))
            var dao = DatabaseHelper.getHelper().getPersonDao()
            CommDBOperation.saveToDB(dao, item)
            showNotification(context!!, register_num, datatype, icon, "")
            EventBus.getDefault().post(register_num)
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