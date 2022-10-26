package com.example.ocr_test


import com.example.ocr_test.model.naver.response.NaverResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface MyApi {
    /** 레트로핏2 코틀린 버전 multipartBody.Part 이슈사항 대처
     * 클로바 API가 문제인지 모르겠지만 Content-Type을 멀티파트로 지정시 작동하지않는다 현재 Headers에서 타입설정을 없앰
     * https://github.com/square/retrofit/issues/3642
     */
    @Headers("X-OCR-SECRET:${BuildConfig.secret_key}")
    @POST("general")
    fun getNaverMultipart(
        @Body message : MultipartBody?
    ) : Call<NaverResponse>



}