package com.mervearas.haritauygulamasi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private  lateinit var  locationManager : LocationManager
    private lateinit var  locationListener : LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(myListener)

        /*

        val amsterdam = LatLng(52.363765,4.8800657) //(enlem,boylam)
        mMap.addMarker(MarkerOptions().position(amsterdam).title("Marker in Amsterdam"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(amsterdam,16f)) // belirtilen konuma zoom yaparak uygulamayı aç zoom oranı float cinsinden verildi (16f şeklinde)
         */

        //KONUM ALIRKEN LOCATIONMANAGER VE LOCATIONLISTENER ÖNEMLİDİR. LocationManager tüm işi yönetirken LocationListener ise konum bilgisini alır.
        //************* (1) **************
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager  //getSystemService = kullanmak istediğimiz servise erişimi sağlar. Biz lokasyona erişmek istiyoruz ve bunu LocationManager olarak tanımlamak için "as" kelimesi kullanıldı.

        // ************* (1) tanımlandı içi sonradan dolduruldu (4) **************
        locationListener = object : LocationListener{
            override fun onLocationChanged(location: Location) {  //Kullanıcının konumu değiştiğinde yapılacaklar buraya yazılır.

                //Aşağıda locationlistener'a aktarılan kullanıcı konum bilgisine zoom yapılarak uygulamayı çalıştırma kodları.
                if(location != null){
                    mMap.clear() //haritaya yeni bir marker eklendiğinde önce diğer markerlar silinir sonra yeni marker eklenir.
                    val userLocation = LatLng(location.latitude,location.longitude) //kullanıcının enlem boylam konumu alındı.
                    mMap.addMarker(MarkerOptions().position(userLocation).title("Your Location"))  //kullanıcının olduğu konuma marker eklendi
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,16f))

                    // ************* (5) ************** KULLANICI HARİTADA BİR YERE BASTIĞINDA ORANIN ADRESİNİ ALMA (geocoder) ***************************

                    val geocoder = Geocoder(this@MapsActivity, Locale.getDefault()) //Locale.getDefault = adres kullanıcı hangi ülkedeyse o ülkenin adres tanımı nasılsa ona göre adresi vermeyi sağlar.
                    try {
                        val addressList = geocoder.getFromLocation(location.latitude,location.longitude,1)  //alının enlem ve boylamdan adres verir.
                        if(addressList != null && addressList.size > 0){
                            println(addressList.get(0).toString())
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }

        // ************* (2) **************KULLANICININ İZİN VERİP VERMEDİĞİ SORGULANIR İZİN VERMEDİYSE İZİN İSTENİR.***************
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){  //konum izni verilmiş mi diye sorgulanır (Permission_Granded izin verildi demek)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)  //Kullanıcıdan konum için izin istenildi
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1f,locationListener)
            //Son bilinen konumu alma
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if(lastLocation != null) {
                val lastKnowLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.addMarker(MarkerOptions().position(lastKnowLocation).title("Your Location"))  //kullanıcının olduğu konuma marker eklendi
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnowLocation,16f))
            }
        }
    }

    // *************** (3) ***************
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //Yukarıda kullanıcıdan izin istendikten sonra hemen yapılması gerekenler buraya yazılır mesela kullanıcı izin verdi kullanıcının konumunu al.
        if(requestCode == 1){
            if(grantResults.size > 0){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1f,locationListener) //konum bilgisi nasıl elde edilsin (GPS), ne kadar sürede bir alınsın (1) ve ne kadar mesafede bir alınsın (1f) ve bu bilgiler nereye aktarılsın (locationlistener) bilgisi girildi
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //***********(6)*********KULLANICI HARİTAYA UZUN BASTIĞINDA YAPILACAKLAR***********************

    val myListener = object : GoogleMap.OnMapLongClickListener{
        override fun onMapLongClick(p0: LatLng?) {
            mMap.clear()
            val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
            if(p0 != null){
                var address = ""
                try {
                    val addressList = geocoder.getFromLocation(p0.latitude,p0.longitude,1)  //verilen enlem ve boylamın adresini alır.
                    if(addressList != null && addressList.size > 0){
                        if(addressList[0].thoroughfare != null) {
                            address += addressList[0].thoroughfare
                            if (addressList[0].subThoroughfare != null){
                                address += addressList[0].subThoroughfare
                            }
                        }
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
                mMap.addMarker(MarkerOptions().position(p0).title(address))
            }else{
                Toast.makeText(applicationContext,"Try Again",Toast.LENGTH_LONG).show()
            }
        }
    }
}