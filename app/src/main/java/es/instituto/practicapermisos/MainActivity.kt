package es.instituto.practicapermisos


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority


class MainActivity : AppCompatActivity(){

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestCamera: ActivityResultLauncher<Void?>

    // Variable de proveedor de ubicación combinada.
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var adaptador: AdaptadorEntrada

    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    private var lastlocation: Location? = null

    // Variables creadas por Mohammed Benali.
    private var TAG = "Permisos"
    private var imagen: Bitmap? = null
    // Variable para actualizar la localizacion solo una vez
    private var solouna = false;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.configLocation()
        this.configRequests()

        adaptador = AdaptadorEntrada(this)

        findViewById<ListView>(R.id.list_view).adapter = adaptador
        // Boton para realizar la peticion
        findViewById<Button>(R.id.b_peticion).setOnClickListener() {

            var permisoconcedido = PackageManager.PERMISSION_GRANTED
            // Guardamos en las variables el resultado de la comprobacion del permiso, -1 si no esta concedido y 0 si esta concedido.
            // Estas variables se van actualizando cada vez que se toca el boton, la primera vez son todas -1 y la segunda vez como ya tenemos
            // los permisos pues se actualizan a 0.
            var permisoCamara = ContextCompat.checkSelfPermission( this, android.Manifest.permission.CAMERA)
            var perUbicacionFine = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            var perUbicacionCoarse =  ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)

            // Comprobamos si los permisos estan concedidos o no.
            if(perUbicacionFine != permisoconcedido && perUbicacionCoarse != permisoconcedido && permisoCamara != permisoconcedido){
                Log.e(TAG,"Solicitando permisos para la camara")
                // Si no lo estan los solicitamos llamando al launcher de los permisos y asignandole que permisos queremos solicitar.
                this.requestPermissionLauncher.launch(
                    arrayOf( android.Manifest.permission.CAMERA,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION))

            }else{
                Log.e(TAG,"Los permisos ya han sido concedidos antes.")
                // Y leemos la ubicacion de forma periódica una vez
                if(!solouna){
                    solouna = true
                    this.fusedLocationClient.requestLocationUpdates(locationRequest!!,locationCallback as LocationCallback,null)
                }
                // Si estan concedidos se la lanza la actividad para tomar la foto llamando al laucher de la camara.
                abrirCamara()
            }
        }

    }
    private fun configLocation() {
        //Es como que nos definimos como cliente para la ubicacion
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //la configuración de la actualización, se asocia a posteriori
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(500)
            .setMaxUpdateDelayMillis(1000)
            .build();

        //que se ejecuta cuando se actualiza
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                //se obtiene la última coordenada
                lastlocation = p0.lastLocation
            }
        }

        //se asocia el servicio al tratamiento de la actualización
        fusedLocationClient.removeLocationUpdates(this.locationCallback as LocationCallback)

    }


    private fun configRequests() {
        // launcher para solicitar permisos
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(),{})

        // Se abre la cámara lanzando el laucher del contrato TakePicturePreview
        requestCamera = registerForActivityResult(ActivityResultContracts.TakePicturePreview()
        ) {
            Toast.makeText(this, "Image capture", Toast.LENGTH_SHORT).show()
            // Guardamos la imagen
            imagen = it!!
            // Creamos una entrada para el adaptador con la imagen y la localizacion y la añadimos al listado
            val entrada=Entrada(imagen!!, this.lastlocation)
            this.adaptador.add(entrada)
        }

    }

    // Metodo para abrir la camara
    private fun abrirCamara(){
        var void : Void? = null
        this.requestCamera.launch(void)
    }

}