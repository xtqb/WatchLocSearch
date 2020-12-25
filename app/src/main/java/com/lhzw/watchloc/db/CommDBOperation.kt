package com.lhzw.watchloc.db

import android.database.sqlite.SQLiteException
import android.text.TextUtils
import android.util.Log
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.stmt.DeleteBuilder
import java.sql.SQLException

/**
 *
@author：created by xtqb
@description:
@date : 2019/10/24 15:22
 *
 */
object CommDBOperation {
    private val TAG = "CommDBOperation"
    fun <T> queryByKeys(dao : Dao<T, Int>, key : String, value : String) : MutableList<T> {
        var list : MutableList<T>? = null
        if(dao != null && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)){
            list =  dao.queryBuilder().where().eq(key, value).query()
        }
        return list!!
    }

    fun <T> saveToDB(dao : Dao<T, Int>, t : T) : Boolean {
        var ret = -1;
        try {
            ret = dao.create(t)
        }catch (e : SQLiteException) {
            Log.e(TAG, e.message)
        }
        return ret > 0
    }

    fun <T> queryAll(dao : Dao<T, Int>, order : String) : MutableList<T> {
        var list : MutableList<T>? = null
        try {
            list = dao.queryBuilder().orderBy(order, false).query()
        }catch (e : SQLException){
            e.printStackTrace()
        }
        return list!!
    }

    fun <T> deleteAll(dao : Dao<T, Int>){
        try {
            var builder  = dao.deleteBuilder()
            builder.setWhere(null)
            builder.delete()
        }catch (e : SQLException){
            e.printStackTrace()
        }
    }

}