package com.latihan.fintechbillingapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class InvoiceActivity : AppCompatActivity() {

    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        // 1. Eksekusi Parsing JSON Payload Transaksi
        val jsonPayload = loadMockInvoiceJson()
        val invoice: InvoiceResponse = Gson().fromJson(jsonPayload, InvoiceResponse::class.java)

        // 2. Tampilkan Header & Custom Date Format
        findViewById<TextView>(R.id.tvInvoiceNo).text = invoice.invoiceNumber
        findViewById<TextView>(R.id.tvTransactionDate).text = formatCustomDate(invoice.transactionDate)
        findViewById<TextView>(R.id.tvStatusBadge).text = invoice.paymentStatus
        findViewById<TextView>(R.id.tvMerchantName).text = invoice.merchant.merchantName
        findViewById<TextView>(R.id.tvCustomerName).text = "Penerima: ${invoice.customer.fullName} (${invoice.customer.phone})"

        // 3. Adapter RecyclerView
        val rvItems = findViewById<RecyclerView>(R.id.rvInvoiceItems)
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = InvoiceItemAdapter(invoice.items)

        // 4. Inisialisasi Elemen Rincian Biaya & Switch Interactive
        val switchVoucher = findViewById<SwitchMaterial>(R.id.switchVoucher)
        val containerVoucher = findViewById<LinearLayout>(R.id.containerVoucher)
        val tvVoucherInfo = findViewById<TextView>(R.id.tvVoucherInfo)
        val rowDiscount = findViewById<View>(R.id.rowDiscount)

        val summary = invoice.summary

        // Fungsi pembaruan angka rincian biaya dinamis
        fun updateSummaryUI(isVoucherActive: Boolean) {
            val discountAmount = if (isVoucherActive) summary.discountAmount else 0.0
            val totalPaid = (summary.subtotalAmount - discountAmount) + summary.taxPpn11 + summary.serviceFee

            if (isVoucherActive && !summary.voucherCode.isNullOrEmpty()) {
                containerVoucher.visibility = View.VISIBLE
                rowDiscount.visibility = View.VISIBLE
                tvVoucherInfo.text = "Promo Terpasang: ${summary.voucherCode} (-${rupiahFormat.format(discountAmount)})"
                findViewById<TextView>(R.id.tvDiscountAmt).text = "- ${rupiahFormat.format(discountAmount)}"
            } else {
                containerVoucher.visibility = View.GONE
                rowDiscount.visibility = View.GONE
            }

            findViewById<TextView>(R.id.tvSubtotalAmt).text = rupiahFormat.format(summary.subtotalAmount)
            findViewById<TextView>(R.id.tvTaxAmt).text = rupiahFormat.format(summary.taxPpn11)
            findViewById<TextView>(R.id.tvFeeAmt).text = rupiahFormat.format(summary.serviceFee)
            findViewById<TextView>(R.id.tvTotalPaid).text = rupiahFormat.format(totalPaid)
        }

        // Set state awal switch berdasarkan ketersediaan voucher
        val hasInitialVoucher = !summary.voucherCode.isNullOrEmpty()
        switchVoucher.isChecked = hasInitialVoucher
        updateSummaryUI(hasInitialVoucher)

        // AKSI 1: Listener Switch Voucher Toggle
        switchVoucher.setOnCheckedChangeListener { _, isChecked ->
            updateSummaryUI(isChecked)
            val msg = if (isChecked) "Voucher promo diterapkan!" else "Voucher promo dilepas"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // AKSI 2: Listener Tombol Salin/Simpan Bukti Transaksi
        findViewById<MaterialButton>(R.id.btnCopyInvoice).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Invoice Number", invoice.invoiceNumber)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "✅ Nomor Bukti ${invoice.invoiceNumber} berhasil disalin!", Toast.LENGTH_LONG).show()
        }
    }

    private fun formatCustomDate(rawDateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy '– Pukul' HH:mm", Locale("id", "ID"))
            val parsedDate = inputFormat.parse(rawDateString)
            parsedDate?.let { outputFormat.format(it) } ?: rawDateString
        } catch (e: Exception) {
            rawDateString
        }
    }

    private fun loadMockInvoiceJson(): String {
        return """{
            "invoice_number": "INV-2026-FT9012",
            "transaction_date": "2026-09-23 10:15:00",
            "payment_status": "PAID_SETTLED",
            "payment_method": "QRIS_BCA",
            "merchant": { 
                "merchant_id": "MCH-99201", 
                "merchant_name": "Mega Elektrindo Ritel", 
                "city_location": "Bandung", 
                "is_verified": true 
            },
            "customer": { 
                "customer_id": "CUST-4412", 
                "full_name": "Ahmad Fauzi", 
                "phone": "081298765432" 
            },
            "items": [
                { "item_id": "ITM-01", "item_name": "Kabel Type-C 65W Fast Charging", "qty": 2, "unit_price": 75000.0, "subtotal": 150000.0 },
                { "item_id": "ITM-02", "item_name": "Adaptor GaN Charger 3-Port 100W", "qty": 1, "unit_price": 320000.0, "subtotal": 320000.0 },
                { "item_id": "ITM-03", "item_name": "Mouse Wireless Silent Click Ergonomis", "qty": 1, "unit_price": 130000.0, "subtotal": 130000.0 }
            ],
            "summary": { 
                "subtotal_amount": 600000.0, 
                "discount_amount": 50000.0, 
                "voucher_code": "DISKON50K",
                "voucher_discount_percent": 10,
                "tax_ppn_11": 60500.0, 
                "service_fee": 2500.0, 
                "total_paid": 613000.0 
            }
        }""".trimIndent()
    }
}