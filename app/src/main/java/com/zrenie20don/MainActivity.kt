package com.zrenie20don

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.wikitude.architect.ArchitectView
import com.wikitude.common.devicesupport.Feature
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mSettings: SharedPreferences? = null
    private var animatorSet: AnimatorSet? = null
    private val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    var editor: SharedPreferences.Editor? = null

    private val pagerAdapter = object : FragmentPagerAdapter(supportFragmentManager) {
        override fun getItem(position: Int): Fragment {
            /*if(position == 1) {
                return new TwoFragment();
            }
            else if(position == 0) {
            */    return ThreeFragment()
            /*}else{
                return new ThreeFragment();
            }*/
        }

        override fun getCount(): Int {
            return 1
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        // Запоминаем данные
        editor = mSettings!!.edit()

        val rUp = findViewById<View>(R.id.rUp) as ImageView
        val rDown = findViewById<View>(R.id.rDown) as ImageView
        val lDown = findViewById<View>(R.id.lDown) as ImageView
        val lUp = findViewById<View>(R.id.lUp) as ImageView

        val logo = findViewById<View>(R.id.logo) as ImageView
        val pager = findViewById<View>(R.id.photos_viewpager) as ViewPager
        //        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
        //
        //            @Override
        //            public void onPageSelected(int arg0) {
        //                if(arg0 == 2){
        //
        //
        //                    new Handler().postDelayed(new Runnable() {
        //                        @Override
        //                        public void run() {
        //                            animatorSet.start();
        //
        //                        }
        //                    },3000);
        //                }
        //            }
        //
        //            @Override
        //            public void onPageScrolled(int arg0, float arg1, int arg2) {
        //                // TODO Auto-generated method stub
        //
        //                System.out.println("onPageScrolled");
        //            }
        //
        //            @Override
        //            public void onPageScrollStateChanged(int num) {
        //                // TODO Auto-generated method stub
        //
        //
        //            }
        //        });

        //TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        // tabLayout.setupWithViewPager(pager);
        animatorSet = AnimatorSet()

        val rUpAnanim = ObjectAnimator.ofFloat(rUp, "translationY", -150f, 0f)
        val rDownAnanim = ObjectAnimator.ofFloat(rDown, "translationY", +150f, 0f)
        val lDownAnanim = ObjectAnimator.ofFloat(lDown, "translationY", +150f, 0f)
        val lUpAnanim = ObjectAnimator.ofFloat(lUp, "translationY", -150f, 0f)

        val logoAnim = ObjectAnimator.ofFloat(logo, "translationY", +300f, 0f)//700

        logoAnim.duration = 2500

        rUpAnanim.duration = 2200
        rDownAnanim.duration = 2300
        lDownAnanim.duration = 2400
        lUpAnanim.duration = 2500


        animatorSet!!.playTogether(
                logoAnim,
                rUpAnanim,
                rDownAnanim,
                lDownAnanim,
                lUpAnanim
        )

        animatorSet!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                rUp.visibility = View.VISIBLE
                rDown.visibility = View.VISIBLE
                lDown.visibility = View.VISIBLE
                lUp.visibility = View.VISIBLE

                logo.visibility = View.VISIBLE
                pager.visibility = View.INVISIBLE
            }


            override fun onAnimationEnd(animator: Animator) {

                if (mSettings!!.contains("start")) {
                    start()
                } else {
                    rUp.visibility = View.INVISIBLE
                    rDown.visibility = View.INVISIBLE
                    lDown.visibility = View.INVISIBLE
                    lUp.visibility = View.INVISIBLE

                    logo.visibility = View.INVISIBLE
                    pager.visibility = View.VISIBLE

                    pager.adapter = pagerAdapter
                }
            }

            override fun onAnimationCancel(animator: Animator) {

            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })

        animatorSet!!.start()

    }

    fun start() {
        editor?.putBoolean("start", true)
        editor?.apply()

        val checkPermission = true

        if (!permissions.fold(true, { acc, it -> acc && ContextCompat.checkSelfPermission(this@MainActivity, it) == PackageManager.PERMISSION_GRANTED })) {
            ActivityCompat.requestPermissions(this@MainActivity, permissions, 1)//выводит диалог, где пользователю предоставляется выбор
        } else {
            //продолжаем работу и вызываем класс
            startActivity(Intent(this@MainActivity, DonArActivity::class.java))
            finish()
        }
    }


    //@Override
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Do task
                    startActivity(Intent(this, DonArActivity::class.java))
                    finish()
                } else {


                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
    }

    companion object {
        val APP_PREFERENCES = "mysettings"
    }
}
