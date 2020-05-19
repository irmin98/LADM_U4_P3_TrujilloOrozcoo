package mx.edu.ittepic.ladm_u4_p3_trujilloorozco

import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    var siPermisoLecturaSMS = 2
    var  siPermisoEnvioSMS =3
    var baseRemota = FirebaseFirestore.getInstance()
    var numeroControl = ""
  var  numeroTel =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_SMS), siPermisoLecturaSMS)
        }

        button.setOnClickListener {

            if(ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.SEND_SMS),siPermisoEnvioSMS)
            }else{
                leerUltimoMensaje()
            }

        }
    }

    private fun leerUltimoMensaje() {
        var cursor = contentResolver.query(
                Uri.parse("content://sms/"),
                null,
                null,
                null,
                null
        )
        var resultado = ""
        if (cursor!!.moveToFirst()) {
            var posColumnaCelularOrigen = cursor.getColumnIndex("address")
            var posColumnaMensaje = cursor.getColumnIndex("body")
            var posColumnaFecha = cursor.getColumnIndex("date")
            // do{

            val fechaMensaje = cursor.getString(posColumnaFecha)
            resultado = "ORIGEN: " + cursor.getString(posColumnaCelularOrigen) +
                    "\nMENSAJE :" + cursor.getString(posColumnaMensaje) +
                    "\nFecha :" + Date(fechaMensaje.toLong()) +
                    "\n--------------------------\n"
            numeroControl = cursor.getString(posColumnaMensaje)
            numeroTel=cursor.getString(posColumnaCelularOrigen)

            //}while (cursor.moveToNext())
            consultarCalif(numeroControl,numeroTel)
        } else {
            resultado = "NO HAY SMS EN BANDEJA DE ENTRADA"
        }
        textView.setText(resultado)
    }

    private fun consultarCalif(n: String, cel:String) {
        var res = ""
        baseRemota.collection("alumno")
                .whereEqualTo("matricula", n)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        //   resultado.setText("ERROR NO HAY CONEXION")
                        var res = "ERROR NO HAY CONEXION"
                        return@addSnapshotListener
                    }

                    for (document in querySnapshot!!) {
                        res += "Alumno: " + document.getString("nombre") +
                                "\nMatricula :" + document.getString("matricula") +
                                "\nCalificacion :" + document.getString("calificacion")

                    }
                        textView2.setText(res)

                    if(!res.equals("")){
                        SmsManager.getDefault().sendTextMessage(cel,null,res,null,null)
                        Toast.makeText(this,"SE ENVIO EL SMS", Toast.LENGTH_LONG).show()
                    }
                }
    }
}
