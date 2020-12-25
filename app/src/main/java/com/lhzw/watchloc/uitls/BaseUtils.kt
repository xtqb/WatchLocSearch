package com.lhzw.watchsearch.uitls

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.text.TextUtils
import android.util.Log
import com.lhzw.watchloc.application.WatchLocApplication
import com.lhzw.watchsearch.constants.Constants
import java.net.NetworkInterface
import java.util.*


/**
 *
@author：created by xtqb
@description:
@date : 2019/10/15 16:09
 *
 */
object BaseUtils {
    private var sp : SharedPreferences? = null
    private fun getSP() : SharedPreferences {
        if(sp == null) {
            sp = WatchLocApplication.getInstance().getApplicationContext().getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE)
        }
        return sp!!
    }

    /**
     * 将获取的4字节定位信息转化为字符串
     */

    fun ByteToStringForLocInfo(data: ByteArray?): Double {
        var floatLocInfo = 0
        if (data == null) {
            return floatLocInfo.toDouble()
        }
        for (i in data.indices) {
            floatLocInfo = floatLocInfo shl 8
            floatLocInfo = floatLocInfo or (data[i].toInt()and 0xff)
        }
        return floatLocInfo / 10000000.0
    }

    // 将byte 字节转化为字符串
    fun traslation(tag_data: ByteArray): String {
        var result = String()
        for (i in tag_data.indices) {
            if (tag_data[i] < 16 && tag_data[i] >= 0) {
                result += "0" + Integer.toHexString(tag_data[i].toInt() and 0xFF)
            } else {
                result += Integer.toHexString(tag_data[i].toInt() and 0xFF)
            }
        }
        return result
    }

    /**
     * 将获取的4字节定位信息转化为字符串
     */

    fun byteArrToTime(data: ByteArray?): Long {
        var floatLocInfo: Long = 0
        var log = ""
        if (data == null) {
            return floatLocInfo
        }
        for (i in data.indices) {
            log += Integer.toHexString(data[i].toInt() and 0xff) + " "
            floatLocInfo = floatLocInfo shl 8
            floatLocInfo = floatLocInfo or (data[i].toInt() and 0xff).toLong()
        }
        Log.e("Tag", "locTime : $log")
        return floatLocInfo * 1000
    }


    fun getPerRegisterByteArr(num: String): ByteArray {
        val byteArr = ByteArray(11)
        System.arraycopy(stringtoByteArr(num), 0, byteArr, 0, 5)
        return byteArr

    }

    /**
     * @param data String translate byte[]
     * @return
     */
    fun stringtoByteArr(data: String): ByteArray? {
        val stringtochar = CharArray(data.length)
        val translat = ByteArray(data.length)
        val result = ByteArray(data.length / 2)
        data.toCharArray(stringtochar, 0, 0, data.length)

        for (j in 0 until data.length) {
            if (stringtochar[j] >= '0' && stringtochar[j] <= '9') {
                translat[j] = (stringtochar[j] - '0').toByte()
            } else if (stringtochar[j] >= 'A' && stringtochar[j] <= 'F') {
                translat[j] = (stringtochar[j] - 'A' + 10).toByte()
            } else if (stringtochar[j] >= 'a' && stringtochar[j] <= 'f') {
                translat[j] = (stringtochar[j] - 'a' + 10).toByte()
            } else {
                return null
            }
        }

        for (i in 0 until data.length / 2) {
            result[i] = ((translat[2 * i].toInt() shl 4) + translat[2 * i + 1]).toByte()
        }
        return result
    }

    fun putString(key: String, value: String) {
        var editor = getSP().edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun getInt(key: String, defaultValue: Int) : Int {
        return getSP().getInt(key, defaultValue)
    }

    fun putInt(key: String, value: Int) {
        var editor = getSP().edit()
        editor.putInt(key, value)
        editor.commit()
    }

    fun getString(key: String, defaultValue: String) : String {
        return getSP().getString(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean){
        var editor = getSP().edit()
        editor.putBoolean(key, value)
        editor.commit()
    }
    fun getBoolean(key: String, defaultValue: Boolean) : Boolean {
        return getSP().getBoolean(key, defaultValue)
    }

    @SuppressLint("WrongConstant")
    fun obtainSearchBytes(): ByteArray? {
        var numStr: String = getString(Constants.BINDING_NUM, "")
        if ("" == numStr || numStr.length != 8) {
            numStr = getMacFromHardware()!!.substring(4)
            putString(Constants.BINDING_NUM, numStr)
        }
        val CHANNEL: Int = getInt(Constants.CHANNEL_NUM, 0)
        val channel: String
        channel = if (CHANNEL == 10) {
            "0" + "a"
        } else {
            "0$CHANNEL"
        }
        numStr = numStr + channel
        return getPerRegisterByteArr(numStr)
    }

    /**
     * 获取手持机的Mac地址
     *
     * @return
     */
    //    private static boolean state = true;
    fun getMacFromHardware(): String? {
//        if (state)
//            return "A4D4B255B58C";
        if (!TextUtils.isEmpty(getString("MAC", ""))) {
            return getString("MAC", "")
        }
        try {
            val manager = WatchLocApplication.instances.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!manager.isWifiEnabled) {
                // 设置为开启状态
                manager.isWifiEnabled = true
            }
            val all: MutableList<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                val macBytes = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.length > 0) {
                    res1.deleteCharAt(res1.length - 1)
                }
                //取消：
                val mac = res1.toString().split(":".toRegex()).toTypedArray()
                val builder = StringBuilder()
                for (str in mac) {
                    builder.append(str)
                }
                all.clear()
                if (builder.toString().length == 12) {
                    putString("MAC", builder.toString())
                    return builder.toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return getMacFromHardware()
    }
}