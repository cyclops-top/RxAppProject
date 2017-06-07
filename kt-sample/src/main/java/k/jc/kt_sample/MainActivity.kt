package k.jc.kt_sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import justin.rxapp.rxApp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById(R.id.test).setOnClickListener {
            rxApp().ensure("android.permission.CAMERA").subscribe {
                Toast.makeText(this, "permission camera $it", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
