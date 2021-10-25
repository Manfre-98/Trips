package com.example.myapplication.businessLogic

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import com.example.myapplication.*
import org.osmdroid.api.IMapController
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*


class LocationImp(context : Context, map : MapView, locationManager : LocationManager) :
    MyLocation {

    private var locationManager: LocationManager? = null
    private var mapController: IMapController? = null
    private var accuracyOverlay : Overlay? = null
    private lateinit var tripTitle : String
    private lateinit var ctx: Context
    private lateinit var startTime : String
    private lateinit var util : MyUtility
    private lateinit var pers : MyPersistence
    private var distanceTraveled : Double = 0.0
    private var elevationGain : Int = 0
    private var follow : Boolean = false
    private var tripStarted : Boolean = false
    private var hasGps = false
    private var hasNetwork = false
    private var myMarker  : Marker? = null
    private var loc: Location? = null
    private var provider : String? = null
    private var mapView : MapView? = null
    private val factory : Factory = Factory()

    init{
        this.locationManager = locationManager
        this.mapView = map
        this.mapController = mapView!!.controller
        this.ctx = context
        util = factory.getUtility()
    }

    @SuppressLint("NewApi")
    override fun saveTrip(){
        //method to save the trip made

        pers = factory.getPersistence(ctx)
        val endTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()+":"+ Calendar.getInstance().get(
            Calendar.MINUTE
        ).toString()
        val dateN = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()+
                "/"+ Calendar.getInstance().get(Calendar.MONTH).toString()+"/"+ Calendar.getInstance().get(
            Calendar.YEAR
        ).toString()

        distanceTraveled /= 1000

        val df = DecimalFormat("##.##")
        df.roundingMode = RoundingMode.CEILING

        if(tripTitle==""){tripTitle=" "}

        val toWrite = tripTitle + "_" + dateN + "_" + startTime + "_" + endTime + "_" + df.format(
            distanceTraveled
        ) + "_" + elevationGain.toString()
        pers.saveTrip(toWrite)


    }

    @SuppressLint("MissingPermission")
    override fun findMe(): Boolean {
        //method to find the current position


        follow = true

        if (tripStarted && loc!=null){
            setLocation(loc!!)
            return true
        }else {

            hasGps = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            hasNetwork = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)


            if (hasGps || hasNetwork) {
                provider = if (hasGps) {
                    LocationManager.GPS_PROVIDER
                } else {
                    LocationManager.NETWORK_PROVIDER
                }

                return if (loc != null &&  util.ageMs(loc!!) < 30000) {
                    setLocation(loc!!)
                    true
                } else {
                    locationManager!!.requestSingleUpdate(provider!!, locationSingleListener, null)
                    true
                }


            }else {return false }
        }

    }

    override fun disableFollow(){
        follow = false
    }
    override  fun disableTrip(){
        tripStarted=false
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setLocation(l: Location){
        //method to set the location on the map
        if(myMarker != null){
            mapView!!.overlays.remove(myMarker)
            //mapView!!.postInvalidate()
        }
        myMarker = Marker(mapView)

        if (accuracyOverlay != null) {
            mapView!!.overlays.remove(accuracyOverlay)
            //mapView!!.postInvalidate()
        }

        if(!tripStarted){
            accuracyOverlay = AccuracyOverlay(GeoPoint(l), l.accuracy)
            mapView!!.overlays.add(accuracyOverlay)
        }

        if(tripStarted){
            myMarker!!.icon = ctx.resources.getDrawable(R.drawable.walk)
        }else myMarker!!.icon = ctx.resources.getDrawable(R.drawable.map_marker)



        myMarker!!.position = GeoPoint(l.latitude, l.longitude)
        myMarker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView!!.overlays.add(myMarker)


        if(follow) {

            if(tripStarted){
                //mapView!!.overlays.add(IconOverlay(GeoPoint(l.latitude, l.longitude), ctx.resources.getDrawable(R.drawable.circle)))
                if(mapView!!.zoomLevelDouble < 18.0){ mapController!!.setZoom(18.0)}
            }else if (mapView!!.zoomLevelDouble  < 15.0) {

                mapController!!.setZoom(15.0)
                //mapView!!.postInvalidate()
            }
            mapController!!.animateTo(GeoPoint(l.latitude, l.longitude))


        }


    }



    private val locationSingleListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            loc = location
            setLocation(location)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }





    @SuppressLint("MissingPermission")
    override fun startTrip(title: String){
        //method to start the trip
        tripTitle = title
        distanceTraveled = 0.0
        elevationGain = 0
        tripStarted = true


        hasGps = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if(!hasGps){

            val gpsOptionsIntent = Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS
            )
            ctx.startActivity(gpsOptionsIntent)
        }else {

            startTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                .toString() + ":" + Calendar.getInstance().get(Calendar.MINUTE).toString()
            follow = true
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10000,
                10F,
                locationListener
            )
        }



    }

    override fun disableUpdate(){
        locationManager!!.removeUpdates(locationListener)
    }


    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if(loc!=null) {
                distanceTraveled += loc!!.distanceTo(location)
                if(location.altitude> loc!!.altitude){
                    elevationGain += (location.altitude - loc!!.altitude).toInt()
                }
            }

            loc = location
            setLocation(location)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
}

class AccuracyOverlay(private val location: GeoPoint?, accuracyInMeters: Float) :
    Overlay() {
    private var paint: Paint? = Paint()
    private var accuracyPaint: Paint? = Paint()
    private val screenCoords: Point = Point()
    private var accuracy = 0f
    override fun onDetach(view: MapView) {
        paint = null
        accuracyPaint = null
    }

    override fun draw(c: Canvas, map: MapView, shadow: Boolean) {
        if (shadow) {
            return
        }
        if (location != null) {
            val pj = map.projection
            pj.toPixels(location, screenCoords)
            if (accuracy > 0) {  //Set this to a minimum pixel size to hide if accuracy high enough
                val accuracyRadius = pj.metersToEquatorPixels(accuracy)

                /* Draw the inner shadow. */accuracyPaint!!.isAntiAlias = false
                accuracyPaint!!.alpha = 30
                accuracyPaint!!.style = Paint.Style.FILL
                c.drawCircle(
                    screenCoords.x.toFloat(),
                    screenCoords.y.toFloat(), accuracyRadius, accuracyPaint!!
                )

                /* Draw the edge. */accuracyPaint!!.isAntiAlias = true
                accuracyPaint!!.alpha = 150
                accuracyPaint!!.style = Paint.Style.STROKE
                c.drawCircle(
                    screenCoords.x.toFloat(),
                    screenCoords.y.toFloat(), accuracyRadius, accuracyPaint!!
                )
            }
        }
    }

    init {
        accuracy = accuracyInMeters
        accuracyPaint!!.strokeWidth = 2F
        accuracyPaint!!.color = Color.BLUE
        accuracyPaint!!.isAntiAlias = true
    }
}

