package com.example.myapplication

interface MyPersistence {
    fun saveTrip(toWrite : String)
    fun findTrips()  : MutableList<String>
    fun removeLine(pos : Int)
}