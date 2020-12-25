package com.lhzw.watchloc.bean

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import com.lhzw.watchsearch.constants.Constants

/**
 *
@author：created by xtqb
@description:
@date : 2019/10/24 14:10
 *
 */
@DatabaseTable(tableName = "PersonInfo")
class PersonInfo {
    @DatabaseField(generatedId = true)
    var id = 0
    @DatabaseField(columnName = "type")
    var type: Int = Constants.PER_COMMON
    @DatabaseField(columnName = "register")
    var register = ""
    @DatabaseField(columnName = "name")
    var name: String = ""
    @DatabaseField(columnName = "age")
    var age: Int = 0
    @DatabaseField(columnName = "gender")
    var gender: String = ""
    @DatabaseField(columnName = "lat")
    var lat: Double = 0.0
    @DatabaseField(columnName = "lng")
    var lng: Double = 0.0
    @DatabaseField(columnName = "locTime")
    var locTime: String = ""

    constructor(type: Int, register: String, name: String = "小明", age: Int, gender: String = "男", lat: Double, lng: Double, locTime: String) {
        this.type = type
        this.register = register
        this.name = name
        this.age = age
        this.gender = gender
        this.lat = lat
        this.lng = lng
        this.locTime = locTime
    }
    constructor(){}
}