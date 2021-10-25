package com.example.myapplication

interface MyLocation {

    fun findMe(): Boolean
    fun startTrip( title : String)
    fun saveTrip()
    fun disableFollow()
    fun disableTrip()
    fun disableUpdate()

}