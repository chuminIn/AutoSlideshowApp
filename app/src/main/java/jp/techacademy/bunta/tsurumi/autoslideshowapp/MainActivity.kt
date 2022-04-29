package jp.techacademy.bunta.tsurumi.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View

import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var mTimer: Timer? = null
    private var mHandler = Handler()
    private var mCursor: Cursor? = null
    private var runflg = false
    private val TIMER_SEC = 2000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start_button.setOnClickListener(this)
        play_stop_button.setOnClickListener(this)
        back_button.setOnClickListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                // getContentsInfo()
                mCursor = getCursor()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            //getContentsInfo()
            mCursor = getCursor()
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            imageView.setImageURI(imageUri)
        }
        cursor.close()
    }

    override fun onClick(v: View) {

        when (v.id){

            R.id.start_button -> {
                if (mCursor != null) {
                    // 最後まで来たらあたまから
                    if (!mCursor!!.moveToNext()) mCursor!!.moveToFirst()
                    showImage()
                }
            }
            R.id.play_stop_button -> {
                if (runflg) {
                    // 実行中なら止める
                    mTimer?.cancel()
                    mTimer = null
                } else {
                    // 停止中なら実行する
                    if (mTimer == null) {
                        mTimer = Timer()
                        mTimer!!.schedule(object : TimerTask() {
                            override fun run() {
                                mHandler.post {
                                    if (mCursor != null) {
                                        showImage()
                                        // 最後まで来たらあたまから
                                        if (!mCursor!!.moveToNext())
                                            mCursor!!.moveToFirst()
                                    }
                                }
                            }
                        }, TIMER_SEC.toLong(), TIMER_SEC.toLong())
                    }
                }
                runflg = !runflg
                setBtnEnable()
            }
            R.id.back_button -> {
                if (mCursor != null) {
                    // 頭なら最後から
                    if (!mCursor!!.moveToPrevious()) mCursor!!.moveToLast()
                    showImage()
                }
            }

        }
    }

    private fun getCursor(): Cursor? {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        return if (cursor!!.moveToFirst()) {
            cursor
        } else {
            cursor.close()
            null
        }
    }

    private fun showImage() {
        if(mCursor != null){
            val fieldIndex = mCursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = mCursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            imageView.setImageURI(imageUri)
        }
    }

    /**
     * ボタンの表示と有効／無効を切り替える
     */
    private fun setBtnEnable() {
        play_stop_button.text = if (runflg) "停止" else "再生"
        back_button.isEnabled = !runflg
        start_button.isEnabled = !runflg
    }
}