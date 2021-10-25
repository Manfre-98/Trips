package com.example.myapplication.persistence

import android.content.Context
import com.example.myapplication.MyPersistence
import java.io.*
import java.util.*

class PersistenceImp(context : Context) : MyPersistence {

    private var ctx : Context? = null

    init {
        this.ctx = context
    }

    override fun saveTrip( toWrite: String) {

        val file = File(ctx!!.filesDir, "History.txt")
        if(!file.exists()){
            file.createNewFile()
        }

        val f = PrintWriter(FileOutputStream(file, true))

        f.println(toWrite)
        f.flush()
        f.close()

    }

    override fun findTrips(): MutableList<String> {
        var list : MutableList<String> = mutableListOf()

        val file = File(ctx!!.filesDir, "History.txt")
        if(file.exists()){

            var riga : String?
            val reader = BufferedReader(FileReader(file))
            riga = reader.readLine()

            while (riga != null){
                val toAdd : String
                val st = StringTokenizer(riga, "_")
                var title = st.nextToken()
                val date = st.nextToken()
                val startTime = st.nextToken()
                val endTime = st.nextToken()
                val distanceTraveled = st.nextToken()
                val elevationGain = st.nextToken()


                if(title == " "){title = "Trip"}

                toAdd = " $title  Date: $date\n Distance traveled: $distanceTraveled km\n Elevation gain: $elevationGain m\n Hour start: $startTime  Hour end: $endTime"

                list.add(toAdd)

                riga = reader.readLine()
            }
            reader.close()
        }
        return list
    }

    override fun removeLine(pos : Int){

        val file = File(ctx!!.filesDir, "temp.txt")
        file.createNewFile()
        val f = PrintWriter(FileOutputStream(file, true))


        val file2 = File(ctx!!.filesDir, "History.txt")


        var riga : String?
        val reader = BufferedReader(FileReader(file2))
        riga = reader.readLine()
        var i = 0

        while (riga != null){
            if(i != pos){
                f.println(riga)
            }
            i++
            riga = reader.readLine()
        }

        reader.close()
        f.close()

        file2.delete()
        file.renameTo(File(ctx!!.filesDir,"History.txt"))



    }


}