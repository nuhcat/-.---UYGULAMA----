package com.example.kantahliliuygulamasi

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class ReminderActivity : AppCompatActivity() {

    private lateinit var tvSelectedDate: TextView
    private lateinit var btnSelectDate: LinearLayout
    private lateinit var btnSetReminder: LinearLayout
    private lateinit var btnCancelReminder: LinearLayout // Yeni iptal butonu

    // Seçilen tarihi saklamak için değişkenler
    private var selectedYear = -1
    private var selectedMonth = -1
    private var selectedDay = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnSelectDate = findViewById(R.id.btnPickDate)
        btnSetReminder = findViewById(R.id.btnSetReminder)
        btnCancelReminder = findViewById(R.id.btnCancelReminder)

        btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        btnSetReminder.setOnClickListener {
            if (selectedYear != -1 && selectedMonth != -1 && selectedDay != -1) {
                setAlarmForSelectedDate(selectedYear, selectedMonth, selectedDay)
            } else {
                Toast.makeText(this, "Lütfen önce tarih seçin!", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelReminder.setOnClickListener {
            cancelAlarm()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, y, m, d ->
            selectedYear = y
            selectedMonth = m
            selectedDay = d
            val selectedDate = "${d}/${m + 1}/$y"
            tvSelectedDate.text = selectedDate
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun setAlarmForSelectedDate(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                Toast.makeText(this, "Exact alarm izni yok!", Toast.LENGTH_SHORT).show()
            }
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        Toast.makeText(this, "Hatırlatıcı ayarlandı!", Toast.LENGTH_SHORT).show()
    }

    private fun cancelAlarm() {
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Toast.makeText(this, "Hatırlatıcı iptal edildi!", Toast.LENGTH_SHORT).show()
    }
}
