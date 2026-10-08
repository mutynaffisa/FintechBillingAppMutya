package com.latihan.fintechbillingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnParse = findViewById<Button>(R.id.btnParseBasic)
        val tvOutput = findViewById<TextView>(R.id.tvOutputBasic)
        val btnOpenInvoice = findViewById<Button>(R.id.btnOpenInvoice)

        // Uji coba parsing dasar (Tahap 1 Modul)
        btnParse.setOnClickListener {
            val rawJsonString = """
                {
                    "merchant_id": "MCH-99201",
                    "merchant_name": "Mega Elektrindo Ritel",
                    "city_location": "Bandung",
                    "is_verified": true
                }
            """.trimIndent()

            val merchantObj: Merchant = Gson().fromJson(rawJsonString, Merchant::class.java)

            tvOutput.text = """
                BERHASIL DI-PARSING:
                • ID Mitra : ${merchantObj.merchantId}
                • Nama Toko: ${merchantObj.merchantName}
                • Kota : ${merchantObj.cityLocation}
                • Terverifikasi: ${if (merchantObj.isVerified) "RESMI (Ya)" else "Belum"}
            """.trimIndent()
        }

        // Navigasi ke Halaman Faktur Pastel (Tahap 2 Modul)
        btnOpenInvoice.setOnClickListener {
            val intent = Intent(this, InvoiceActivity::class.java)
            startActivity(intent)
        }
    }
}