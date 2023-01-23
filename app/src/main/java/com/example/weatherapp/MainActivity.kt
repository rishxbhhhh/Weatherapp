package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


//Rishabh Rajpurohit | 2023
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    var CITY: String = "delhi,in"
    val APIKEY: String = BuildConfig.APK_KEY

    private lateinit var updateCityBtn: Button
    private lateinit var updateCityField: EditText


    private lateinit var refreshLayout: SwipeRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //This code is used to make the statusBar and Bottom NavigationBar match the color of the app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.gradient_end)
            window.navigationBarColor = resources.getColor(R.color.gradient_start)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }



        //This is used to add City Input functionality
        updateCityField = findViewById<EditText?>(R.id.updateCityField)
        updateCityField.hint = "Enter City Name"
        updateCityBtn = findViewById(R.id.updateCityBtn)
        updateCityBtn.setOnClickListener {
            var city = findViewById<EditText>(R.id.updateCityField).text.toString()
            if(city!=null) CITY=city
            updateCityField.clearFocus()
            weatherTask().execute()
            val inputManager: InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0)
            updateCityField.setText("")
        }

        //This is used to add swipe refresh functionality
        refreshLayout = findViewById(R.id.swipe_refresh_layout)
        refreshLayout.setOnRefreshListener {
            weatherTask().execute()
            updateCityField.setText("")
        }
        //This is the initial call of the app.
        weatherTask().execute()
    }

    override fun onRestart() {
        super.onRestart()
        weatherTask().execute()
        updateCityField.setText("")
    }
    inner class weatherTask() : AsyncTask<String, Void, String>()
    {

        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errortext).visibility = View.GONE
        }
        override fun doInBackground(vararg p0: String?): String? {
            var response:String?
            try {
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$APIKEY").readText(Charsets.UTF_8)
            }
            catch (e: Exception)
            {
                response = null
            }

            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weatherArray = jsonObj.getJSONArray("weather")
                val weather = if (weatherArray.get(0) is JSONObject) weatherArray.getJSONObject(0)
                else JSONObject().put("weather",weatherArray.get(0))
                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: "+SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))
                val temp = main.getString("temp")+"°C"
                val tempMin = "Min: "+main.getString("temp_min")+"°C"
                val tempMax = "Max: "+main.getString("temp_max")+"°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")
                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")
                val address = jsonObj.getString("name")+", "+sys.getString("country")

                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text = updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity

                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

                val githubProfile = "https://github.com/rishxbhhhh"
                val link = findViewById<LinearLayout>(R.id.devProfile)
                link.setOnClickListener {
                    val openURL = Intent(Intent.ACTION_VIEW)
                    openURL.data = Uri.parse(githubProfile)
                    startActivity(openURL)
                }

            }
            catch (e: Exception)
            {

                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                //findViewById<TextView>(R.id.errortext).text = e.toString() //this logs the error
                findViewById<TextView>(R.id.errortext).visibility = View.VISIBLE

            }
            refreshLayout.isRefreshing = false
        }
    }
}