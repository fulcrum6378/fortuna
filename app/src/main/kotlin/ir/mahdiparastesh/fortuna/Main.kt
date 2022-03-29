package ir.mahdiparastesh.fortuna

import android.os.Bundle
import androidx.activity.ComponentActivity
import ir.mahdiparastesh.fortuna.databinding.MainBinding

class Main : ComponentActivity() {
    private lateinit var b: MainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = MainBinding.inflate(layoutInflater)
    }
}
