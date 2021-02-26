package com.elds.handson

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView

class HomeActivity : AppCompatActivity() {

    private lateinit var cardRecImagem: CardView
    private lateinit var cardRecFamoso: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        cardRecImagem = findViewById(R.id.reconhecimentoImagemId)
        cardRecFamoso = findViewById(R.id.reconhecimentoFamosoId)

        cardRecImagem.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

//        cardRecFamoso.setOnClickListener {
//            val intent = Intent(this, ::class.java)
//            startActivity(intent)
//        }

    }
}