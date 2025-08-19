package com.example.kantahliliuygulamasi

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kantahliliuygulamasi.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding.buttonOCR.setOnClickListener {
            startActivity(Intent(this, OcrActivity::class.java))
        }

        binding.buttonHistory.setOnClickListener {
            startActivity(Intent(this, GecmisTestlerActivity::class.java))
        }

        binding.buttonMap.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        binding.buttonReminder.setOnClickListener {
            startActivity(Intent(this, ReminderActivity::class.java))
        }

        binding.buttonBMI.setOnClickListener {
            startActivity(Intent(this, BMIActivity::class.java))
        }

        binding.buttonLogout.setOnClickListener {
            auth.signOut()
            sharedPref.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            setAlarmForSelectedDate(selectedYear, selectedMonth, selectedDay)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun setAlarmForSelectedDate(selectedYear: Int, selectedMonth: Int, selectedDay: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
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
}
