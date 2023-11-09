package com.sonchan.gps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonchan.gps.databinding.ActivityWeatherBinding
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class Weather : AppCompatActivity(){

    val response:WeatherRes.Response
        get() {
            TODO()
        }

    // 뷰모델 생성
    private val viewModel by viewModels<WeatherViewModel>()

    private lateinit var binding: ActivityWeatherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 데이터 바인딩
        binding = DataBindingUtil.setContentView(this, R.layout.activity_weather)
        binding.lifecycleOwner = this

        viewModel.getWeather("JSON", 10, 1,
            20231109, 1100, "57", "74" )

        viewModel.weatherResponse.observe(this){
            for(i in it.body()?.response!!.body.items.item){
                Log.d(Tag, "%i")
            }
        }
    }

    companion object{
        // API값
        const val API_KEY = "3dfbOy4Hl2E9UyZPfME%2FMC%2BXAf4%2BhaCppjq5g%2Bc%2FvNr%2Bam18%2B8%2FCskd%2FHmYsBhCAMGt8cYa71Rdsssa%2FmYWQUg%3D%3D"
        // Call Back URL
        const val BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst"
        const val Tag = "test"
    }
}

// 요청 메시지 명세
interface WeatherApi{
    @GET("getVilageFcst?serviceKey=${Weather.API_KEY}")
    suspend fun getWeather(
        @Query("dataType") dataType:String,
        @Query("numOfRows") numOfRows:Int,
        @Query("pageNo") pageNo:Int,
        @Query("base_data") baseData:Int,
        @Query("base_time") baseTime:Int,
        @Query("nx") nx:String,
        @Query("ny") ny:String
    ) : Response<Weather>
}

data class WeatherRes(
    val response: Response
){
    data class Response(
        val header:Header,
        val body: Body
    )

    data class Header(
        val resultCode:Int,
        val resultMsg:String
    )

    data class Body(
        val dataType: String,
        val items: Items
    )

    data class Items(
        val item:List<Item>
    )

    data class Item(
        val baseData: Int,
        val baseTime: Int,
        val category: String,
        val fcstData: Int,
        val fcstTime: Int,
        val fcstValue: String,
        val nx:Int,
        val ny:Int
    )
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule{
    @Provides
    @Singleton
    fun provideRetrofit() : Retrofit =
        Retrofit.Builder().baseUrl(Weather.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()

    @Provides
    @Singleton
    fun provideWeatherApi(retrofit: Retrofit) : WeatherApi =
        retrofit.create(WeatherApi::class.java)
}

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApi
){
    // Repository에서 날씨 API를 직접 요청하기 위한 getWeather 메소드
    suspend fun getWeather(
        dataType:String, numOfRows: Int, pageNo: Int,
        baseData: Int, baseTime: Int, nx: String, ny: String) : Response<Weather>{
        return weatherApi.getWeather(dataType, numOfRows, pageNo, baseData, baseTime, nx, ny)
    }
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {
    private val _weatherResponse : MutableLiveData<Response<Weather>> = MutableLiveData()
    val weatherResponse get() = _weatherResponse

    fun getWeather(dataType: String, numOfRows: Int, pageNo: Int,
                   baseData: Int, baseTime: Int, nx: String, ny:String){
        viewModelScope.launch {
            val response = repository.getWeather(dataType, numOfRows, pageNo, baseData, baseTime, nx, ny)
            _weatherResponse.value = response
        }
    }
}