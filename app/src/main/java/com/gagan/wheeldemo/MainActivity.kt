package com.gagan.wheeldemo

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gagan.wheeldemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

            val bitmaps: MutableList<Int> = ArrayList()
            bitmaps.add(R.drawable.tatoo_1)
            bitmaps.add(R.drawable.tatoo_2)
            bitmaps.add(R.drawable.tatoo_3)
            bitmaps.add(R.drawable.tatoo_4)
            bitmaps.add(R.drawable.tatoo_5)
            bitmaps.add(R.drawable.tatoo_6)
            wheelSpinner.setArrowPointer(ivArrow)
            wheelSpinner.setBitmapsId(bitmaps)
            wheelSpinner.setOnItemSelectListener(object : OnItemSelectListener {
                override fun onTattooSelected(bitmap: Bitmap?) {
                    ivSelectedTattoo.setImageBitmap(bitmap)
                    ivSelectedTattoo.visibility = View.VISIBLE
                }
            })

            btnSpin.setOnClickListener{
                ivSelectedTattoo.visibility = View.GONE
                wheelSpinner.rotateWheel()
            }
        }
    }
}