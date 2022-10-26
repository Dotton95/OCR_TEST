package com.example.ocr_test

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.ocr_test.model.naver.response.NaverResponse
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    val TAG = "dotton95"
    val PERMISSION_CHECK_LIST: Array<String> = arrayOf( // 공통으로 받는 초기 권한 목록
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var currentPhotoPath = ""

    var useImage : Bitmap? = null //사용되는 이미지
    var tess:TessBaseAPI?=null
    var datapath = ""
    private val langFileName = "kor.traineddata"

    var resutText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //퍼미션 권한 받기
        initCheckPermission()
        
        //바로 카메라 연동
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile:File? = try {
                    createImageFile()
                }catch (ex:IOException) {
                    ex.printStackTrace()
                    null
                }
                photoFile?.also {
                    val photoURI : Uri = FileProvider.getUriForFile(this,"com.example.ocr_test.fileprovider",it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI)
                    startActivityForResult(takePictureIntent, 1)
                }
            }
        }

        datapath = "$filesDir/tesseract/"
        checkFile(File(datapath+"tessdata/"))

        tess = TessBaseAPI()
        tess!!.init("$filesDir/tesseract/","kor")

        var intent = Intent(this,MainActivity2::class.java)

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        Log.d(TAG,"x - ${size.x.toString()}")
        Log.d(TAG,"y - ${size.y.toString()}")

        /** 테서렉트 OCR */
        btn_tesseract.setOnClickListener {
            processImage(it)

            intent.putExtra("data",resutText)
            startActivity(intent)
        }
        /** 네이버 OCR */
        btn_naver.setOnClickListener {
            /** CLOVA OCR API EXAMPLE
            var thread = Thread{
                HttpConnection().connection(file.absolutePath)
            }
            thread.start()
            */

            var file = File(currentPhotoPath)

            val jsonObject = JSONObject("{\"images\":[{\"format\":\"jpg\",\"name\":\"demo\"}],\"requestId\":\"guide-demo\",\"version\":\"V2\",\"timestamp\":0}")
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message",jsonObject.toString())
                .addFormDataPart("file",file.name,file.asRequestBody())
                .build()

            val api = RetrofitClient.getNaverRetrofit()

            api.getNaverMultipart(multipartBody)
                .enqueue(object : Callback<NaverResponse?> {
                    override fun onResponse(
                        call: Call<NaverResponse?>,
                        response: Response<NaverResponse?>
                    ) {
                        if(response.isSuccessful){
                            if(response.code()==200){
                                val size =  response.body()!!.images[0].fields.size
                                var result = ""
                                for(i in 0 until size){
                                    result += response.body()!!.images[0].fields[i].inferText+"\n"
                                }
                                resutText = result
                                intent.putExtra("data",resutText)
                                startActivity(intent)

                            }else Log.d(TAG,"응답했으나 not Sucess Code - code: ${response.code()}")
                        }else{
                            Log.d(TAG,"Retrofit2 Fail getNaverMultipart" +
                                "\n================================================" +
                                        "\n\t응답 에러 발생    - getNaverMultipart" +
                                        "\n\t응답 에러 메세지  - ${response.message()}" +
                                        "\n\t응답 에러 코드    - ${response.code()}"+
                                    "\n================================================")
                        }
                    }

                    override fun onFailure(call: Call<NaverResponse?>, t: Throwable) {
                        t.printStackTrace()
                        Log.d(TAG,"응답 실패 - ${t.message}")
                    }
                })


        }
        /** 카카오 OCR */
        btn_kakao.setOnClickListener { 
            
        }
        /** 구글 OCR */
        btn_google.setOnClickListener {

        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == RESULT_OK) {
            val file = File(currentPhotoPath)
            if(Build.VERSION.SDK_INT < 28){
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,Uri.fromFile(file))
                image.setImageBitmap(bitmap)
                useImage = bitmap.copy(Bitmap.Config.ARGB_8888,true)
            }else {
                val decode = ImageDecoder.createSource(this.contentResolver,Uri.fromFile(file))
                val bitmap = ImageDecoder.decodeBitmap(decode)
                image.setImageBitmap(bitmap)
                useImage = bitmap.copy(Bitmap.Config.ARGB_8888,true)
            }
        }
    }


    private fun initCheckPermission() {
        if (!checkPermissions(PERMISSION_CHECK_LIST)) requestPermissions(PERMISSION_CHECK_LIST, 999)
    }
    private fun checkPermissions(permissions: Array<String>): Boolean {
        return permissions.all {ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            999 -> {
                if (grantResults.contains(-1)) {
                    Toast.makeText(applicationContext,
                        "[앱설정 > 권한]에서\n거부된 권한을 확인해주세요.",
                        Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    fun processImage(view: View?) {
        if(useImage!=null){
            Log.d("dotton","useImage null 아님")
            tess!!.setImage(useImage)
            resutText = tess!!.utF8Text
        }else{
            Log.d("dotton","useImage null 임")
        }
    }


    @Throws(IOException::class)
    private fun createImageFile() : File {
        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir : File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "${timeStamp}",
            ".jpg",
            storageDir
        ).apply{
            currentPhotoPath = absolutePath
        }
    }

    private fun copyFiles() {
        try {
            val filepath = datapath + "tessdata/" + langFileName
            val assetManager = assets
            val instream: InputStream = assetManager.open(langFileName)
            val outstream: OutputStream = FileOutputStream(filepath)
            val buffer = ByteArray(1024)
            var read: Int
            while (instream.read(buffer).also { read = it } != -1) {
                outstream.write(buffer, 0, read)
            }
            outstream.flush()
            outstream.close()
            instream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun checkFile(dir: File) {
        if (!dir.exists() && dir.mkdirs()) {//디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
            copyFiles()
        }

        if (dir.exists()) {
            val datafilepath = datapath + "tessdata/" + langFileName
            val datafile = File(datafilepath)
            if (!datafile.exists()) {//디렉토리가 있지만 파일이 없으면 파일카피 진행
                copyFiles()
            }
        }
    }

}