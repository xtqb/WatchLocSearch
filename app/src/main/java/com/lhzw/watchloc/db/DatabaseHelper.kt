package com.lhzw.watchloc.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import com.lhzw.watchloc.application.WatchLocApplication
import com.lhzw.watchloc.bean.PersonInfo
import com.lhzw.watchsearch.constants.Constants

/**
 *
@author：created by xtqb
@description:
@date : 2019/10/24 14:15
 *
 */

class DatabaseHelper(mContext: Context) : OrmLiteSqliteOpenHelper(mContext, Constants.DB_NAME, null, Constants.DB_VERSION) {

    private var personDao: Dao<PersonInfo, Int>? = null
    companion object {
        private var instance: DatabaseHelper? = null
        fun getHelper() : DatabaseHelper  {
            synchronized(DatabaseHelper.javaClass) {
                if (instance == null) {
                    instance = DatabaseHelper(WatchLocApplication.getInstance().applicationContext)
                }
            }
            return instance!!
        }
    }

    override fun onCreate(p0: SQLiteDatabase?, p1: ConnectionSource?) {
        // 创建表
        TableUtils.createTable<PersonInfo>(p1, PersonInfo::class.java)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: ConnectionSource?, oldVersion: Int, newVersion: Int) {

    }

    fun getPersonDao() : Dao<PersonInfo, Int>  {
        if(personDao == null) {
            personDao = getDao(PersonInfo::class.java)
        }
        return personDao!!
    }

}