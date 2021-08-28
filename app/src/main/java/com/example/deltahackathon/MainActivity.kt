package com.example.deltahackathon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.deltahackathon.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.gameView.PlayerThread().start()
        binding.gameView.msalThread().start()
        binding.gameView.groundThread().start()
    }

}