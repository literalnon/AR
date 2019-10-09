package com.zrenie20don;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences mSettings;
    private AnimatorSet animatorSet;
    public static final String APP_PREFERENCES = "mysettings";
    private final String[] permissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION

    };
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        // Запоминаем данные
        editor = mSettings.edit();

        final ImageView rUp = (ImageView) findViewById(R.id.rUp);
        final ImageView rDown = (ImageView) findViewById(R.id.rDown);
        final ImageView lDown = (ImageView) findViewById(R.id.lDown);
        final ImageView lUp = (ImageView) findViewById(R.id.lUp);

        final ImageView logo = (ImageView) findViewById(R.id.logo);
        final ViewPager pager = (ViewPager) findViewById(R.id.photos_viewpager);
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
        animatorSet = new AnimatorSet();
        Animator rUpAnanim = new ObjectAnimator().ofFloat(rUp, "translationY", -150, 0);
        Animator rDownAnanim = new ObjectAnimator().ofFloat(rDown, "translationY", +150, 0);
        Animator lDownAnanim = new ObjectAnimator().ofFloat(lDown, "translationY", +150, 0);
        Animator lUpAnanim = new ObjectAnimator().ofFloat(lUp, "translationY", -150, 0);

        Animator logoAnim = new ObjectAnimator().ofFloat(logo, "translationY", +700, 0);
        logoAnim.setDuration(2500);

        rUpAnanim.setDuration(2200);
        rDownAnanim.setDuration(2300);
        lDownAnanim.setDuration(2400);
        lUpAnanim.setDuration(2500);


        animatorSet.playTogether(
                logoAnim,
                rUpAnanim,
                rDownAnanim,
                lDownAnanim,
                lUpAnanim
        );

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                rUp.setVisibility(View.VISIBLE);
                rDown.setVisibility(View.VISIBLE);
                lDown.setVisibility(View.VISIBLE);
                lUp.setVisibility(View.VISIBLE);

                logo.setVisibility(View.VISIBLE);
                pager.setVisibility(View.INVISIBLE);
            }


            @Override
            public void onAnimationEnd(Animator animator) {

                if(mSettings.contains("start")){
                    start();
                }else{
                    rUp.setVisibility(View.INVISIBLE);
                    rDown.setVisibility(View.INVISIBLE);
                    lDown.setVisibility(View.INVISIBLE);
                    lUp.setVisibility(View.INVISIBLE);

                    logo.setVisibility(View.INVISIBLE);
                    pager.setVisibility(View.VISIBLE);

                    pager.setAdapter(pagerAdapter);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        animatorSet.start();

    }

    public void start(){
        editor.putBoolean("start", true);
        editor.apply();

        if(ContextCompat.checkSelfPermission(MainActivity.this,  android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);//выводит диалог, где пользователю предоставляется выбор
        }else{
            //продолжаем работу и вызываем класс
            startActivity(new Intent(MainActivity.this, DonArGeoActivity.class));
            finish();

        }
    }

    private FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            /*if(position == 1) {
                return new TwoFragment();
            }
            else if(position == 0) {
            */    return new ThreeFragment();
            /*}else{
                return new ThreeFragment();
            }*/
        }

        @Override
        public int getCount() {
            return 1;
        }

    };


    //@Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    // Do task
                    startActivity(new Intent(this, DonArGeoActivity.class));
                    finish();
                } else {


                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
}
