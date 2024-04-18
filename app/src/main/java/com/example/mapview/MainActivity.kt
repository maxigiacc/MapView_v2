package com.example.mapview

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.bonuspack.routing.RoadNode
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    // le chiamate di rete saranno tutte fatte nel main thread --> NON SI FA
    // successivamente, v3, si inseriranno dentro i thread

    //Tutorial_0
    private lateinit var map: MapView
    private lateinit var startPoint: GeoPoint
    private lateinit var mapController: IMapController
    private lateinit var startMarker: Marker

    //Tutorial_1
    private lateinit var roadManager: RoadManager
    private var points: ArrayList<GeoPoint> = ArrayList()
    private lateinit var endPoint: GeoPoint
    private lateinit var road: Road
    private lateinit var roadOvelay: Polyline
    private lateinit var nodeMarker : Marker
    private lateinit var node : RoadNode

    companion object {
        var MY_USER_AGENT = "MyOwnUserAgent/1.0"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        Configuration.getInstance().userAgentValue = MY_USER_AGENT
        setContentView(R.layout.activity_main)
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        startPoint = GeoPoint(44.49502448619586, 11.34244909069136)
        mapController = map.controller
        mapController.setZoom(15)
        mapController.setCenter(startPoint)

        startMarker = Marker(map)
        startMarker.position = startPoint
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.add(startMarker)

        map.invalidate()
        startMarker.icon = resources.getDrawable(R.drawable.ic_pos)
        startMarker.setTitle("Start point")

        //Tutorial_1
        roadManager = OSRMRoadManager(this, MY_USER_AGENT)
        points.add(startPoint)
        endPoint = GeoPoint(44.49952139754052, 11.347525004151265)
        points.add(endPoint)

        road = roadManager.getRoad(points)
        roadOvelay = RoadManager.buildRoadOverlay(road)

        //insert roadtrip
        map.overlays.add(roadOvelay)

        //refresh map
        map.invalidate()

        (roadManager as OSRMRoadManager).setMean(OSRMRoadManager.MEAN_BY_CAR)

        val nodeIcon = ResourcesCompat.getDrawable(resources, R.drawable.marker_node, null)
        for (i in 0 until road.mNodes.size) {
            val node = road.mNodes[i]
            Timber.d("de", node.toString())
            val nodeMarker = Marker(map)
            Timber.d("de", nodeMarker.toString())
            nodeMarker.position = node.mLocation
            Log.d("Coordinate", "Latitudine: ${node.mLocation.latitude}, Longitudine: ${node.mLocation.longitude}")
            nodeIcon?.let { nodeMarker.icon = BitmapDrawable(resources, Bitmap.createBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888)) }
            nodeMarker.title = "Step $i"
            Log.d("NodeMarker", "$nodeMarker")
            nodeMarker.icon = nodeIcon
            map.overlays.add(nodeMarker)
            Log.d("Overlay", "Numero di Overlay: ${map.overlays.size}")
            nodeMarker.snippet = node.mInstructions
            nodeMarker.subDescription = Road.getLengthDurationText(this, node.mLength, node.mDuration)
            val icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_continue, null)
            icon?.let { nodeMarker.image = BitmapDrawable(resources, Bitmap.createBitmap(it.intrinsicWidth, it.intrinsicHeight, Bitmap.Config.ARGB_8888)) }
        }

    }
}


