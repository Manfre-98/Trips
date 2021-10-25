package com.example.myapplication

import android.content.Context
import android.location.LocationManager
import com.example.myapplication.businessLogic.LocationImp
import com.example.myapplication.persistence.PersistenceImp
import com.example.myapplication.businessLogic.UtilityImp
import org.osmdroid.views.MapView

class Factory {

    fun getLocation(context : Context, map : MapView, locationManager : LocationManager) : MyLocation{
        var retVal : MyLocation? = null

        retVal = LocationImp(context, map, locationManager)

        return retVal

    }

    fun getPersistence(context : Context): MyPersistence{
        var retVal : MyPersistence? = null

        retVal = PersistenceImp(context)

        return retVal
    }

    fun getUtility() : MyUtility{

        var retVal : MyUtility? = null

        retVal = UtilityImp()

        return retVal

    }
}