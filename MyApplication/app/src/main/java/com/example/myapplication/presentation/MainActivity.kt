package com.example.myapplication.presentation

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.myapplication.*
import com.example.myapplication.external.MyCompassOverlay
import com.example.myapplication.external.MyScaleBarOverlay
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider


private const val REQUEST_PERMISSIONS_REQUEST_CODE = 1


class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var mapController: IMapController
    private lateinit var tripTitle : String
    private lateinit var ctx: Context
    private var hasGps = false
    private var ok : Boolean = false
    private lateinit var locMe : MyLocation

    private var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //request permissions
        requestPermissionsIfNecessary(permissions)

        //wait the permissions
        while(!ok) {
            var find = false
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    find = true
                }
            }
            ok = !find
        }


        ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(R.layout.activity_main)
        val factory = Factory()


        btn_stop_trip.isEnabled = false
        btn_stop_trip.visibility = View.GONE

        //set the map
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(false)


        //scale bar
        val dm = ctx.resources.displayMetrics
        val mScaleBarOverlay =
            MyScaleBarOverlay(mapView)
        mScaleBarOverlay.setAlignBottom(true)



       //play around with these values to get the location on screen in the right place for your application
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
        mapView.overlays.add(mScaleBarOverlay)

        //set the compass
        val compassOverlay = MyCompassOverlay(
            ctx,
            InternalCompassOrientationProvider(ctx),
            mapView
        )
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)


        //set the home point
        mapController = mapView.controller
        mapController.setZoom(6.5)
        val startPoint = GeoPoint(42.66809381102138, 12.890208341959477)
        mapController.setCenter(startPoint)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locMe = factory.getLocation(ctx, mapView, locationManager)


        //set the callback for the buttons
        btn_get_loc.setOnClickListener{
            if(!locMe.findMe()){requestOption("")}
        }

        btn_stop_trip.setOnClickListener{
            requestConfirm()
        }

    }



    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //method to stop location tracking
        if (event!= null ) {
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                locMe.disableFollow()
            }
        }
        return false
    }




    override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        mapView.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        mapView.onPause() //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //method used for the menu/action bar
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //method used when clicking on option of the menu/action bar
        when (item.itemId) {
            R.id.MENU_1 -> {
                hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

                if (!hasGps) {
                    requestOption("GPS")
                } else {
                    requestTitle()
                }
            }
            R.id.MENU_2 -> {
                val i = Intent(this, HistoryActivity::class.java)
                startActivity(i)
            }
            R.id.MENU_3 -> {
                val ii = Intent(this, AboutActivity::class.java)
                startActivity(ii)
            }
        }
        return false
    }

    private fun requestOption(type: String){
        //method to request the activation of the gps
        val mBuilderOption = AlertDialog.Builder(this)
        if(type == "GPS") {
            mBuilderOption.setTitle("Need GPS")
            mBuilderOption.setMessage("If you want to take a new trip you have to enable Gps")
        }else{mBuilderOption.setTitle("Need Geolocation")
            mBuilderOption.setMessage("If you want to locate, you have to enable Geolocation")}

        mBuilderOption.setPositiveButton(
            "OK"
        ) { _, _ ->
                val optionsIntent = Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                )
                startActivity(optionsIntent)
        }
        mBuilderOption.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }

        mBuilderOption.show()

    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        //method to check and request permission
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestTitle(){
        //method to request title and start the new trip
        val textInputLayout = TextInputLayout(this)
        textInputLayout.setPadding(
            50,
            0,
            50,
            0
        )

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("New Trip")
        builder.setMessage("Write a title for the new trip if you want")

        tripTitle = ""

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        textInputLayout.hint = "Name"
        textInputLayout.addView(input)


        builder.setView(textInputLayout)

        // Set up the buttons

        // Set up the buttons
        builder.setPositiveButton(
            "OK"
        ) { _, _ -> //tripTitle = input.text.toString()
                    locMe.startTrip(input.text.toString())
                    btn_stop_trip.isEnabled = true
                    btn_stop_trip.visibility = View.VISIBLE

        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun requestConfirm(){
        //method to request confirm to end the trip
        val listItems = arrayOf("Save", "Don't save")
        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setTitle("Save Trip?")

        var inputSelection =1

        mBuilder.setSingleChoiceItems(listItems, -1) { _, i ->
            inputSelection = i
        }

        mBuilder.setPositiveButton(
            "OK"
        ) { _, _ ->
            if (inputSelection==0){
                Toast.makeText(ctx, "Saved!", Toast.LENGTH_SHORT).show()
                locMe.saveTrip()
            }else { Toast.makeText(ctx, "Don't saved!", Toast.LENGTH_SHORT).show()}

            locMe.disableUpdate()
            btn_stop_trip.isEnabled = false
            btn_stop_trip.visibility = View.GONE
            locMe.disableTrip()
            locMe.findMe()
        }
        mBuilder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }

        val mDialog = mBuilder.create()
        mDialog.show()

    }

}