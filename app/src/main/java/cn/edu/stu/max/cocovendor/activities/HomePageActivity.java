package cn.edu.stu.max.cocovendor.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.io.File;
import java.util.List;

import cn.edu.stu.max.cocovendor.databaseClass.CabinetDailySales;
import cn.edu.stu.max.cocovendor.databaseClass.CabinetMonthlySales;
import cn.edu.stu.max.cocovendor.javaClass.FileService;
import cn.edu.stu.max.cocovendor.adapters.GridViewAdapter;
import cn.edu.stu.max.cocovendor.javaClass.Model;
import cn.edu.stu.max.cocovendor.javaClass.ToastFactory;
import cn.edu.stu.max.cocovendor.R;
import cn.edu.stu.max.cocovendor.databaseClass.Goods;
import cn.edu.stu.max.cocovendor.databaseClass.Sales;
import cn.edu.stu.max.cocovendor.javaClass.ViewHolder;
import cn.edu.stu.max.cocovendor.adapters.ViewPagerAdapter;

public class HomePageActivity extends SerialPortActivity {

    SharedPreferences cabinetDailySalesSharedPreference;
    SharedPreferences.Editor editor;

    private static final int SECONDS_OF_AD = 60;    // 60秒无操作后自动进入全屏广告播放模式

    private final static String TOPATH = "/storage/sdcard0/tencent/QQfile_recv/b/";               // 本机广告存储路径

    private static final int REQUEST_CODE_1 = 1;
    private static final int REQUEST_PAY_RESULT_CODE = 2;

    private VideoView videoViewHomePageAd;
    private ImageView imageViewHomePageAd;

    private static int videoFileIndex = 0;
    private static int[] videoListOrder;
    private static int[] videoListFrequency;
    private static int tempFrequency = 0;

    private SharedPreferences share;

    private static final String adSettingDataFileName = "sharedfile";     // 定义保存的文件的名称

    private Context context;

    Handler handler = new Handler();

    private TextView textViewCoinMoney;

    private static final byte[] coinMoney = {0, 0, 0, '.', 0, 0};

    private ViewPager mPager;
    private List<View> mPagerList;
    private List<Model> mDatas;
    private LinearLayout mLlDot;
    private LayoutInflater inflater;
    /**
     * 总的页数
     */
    private int pageCount;
    /**
     * 每一页显示的个数
     */
    private int pageSize = 6;
    /**
     * 当前显示的是第几页
     */
    private int curIndex = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_home_page);

        mPager = (ViewPager) findViewById(R.id.viewpager);
        mLlDot = (LinearLayout) findViewById(R.id.ll_dot);

        mPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: { // 手指下来的时候,取消之前绑定的Runnable

                        handler.removeCallbacks(runnable);

                        break;
                    }
                    case MotionEvent.ACTION_UP: { // 手指离开屏幕，发送延迟消息 ，5秒后执行

                        handler.postDelayed(runnable, 1000 * SECONDS_OF_AD);

                        break;
                    }
                }
                return false;
            }
        });

        //初始化数据源
        initDatas();
        inflater = LayoutInflater.from(this);
        //总的页数=总数/每页数量，并取整
        pageCount = (int) Math.ceil(mDatas.size() * 1.0 / pageSize);
        mPagerList = new ArrayList<View>();
        try {
            for (int i = 0; i < pageCount; i++) {
                //每个页面都是inflate出一个新实例
                GridView gridView = (GridView) inflater.inflate(R.layout.gridview, mPager, false);
                gridView.setAdapter(new GridViewAdapter(this, mDatas, i, pageSize));
                mPagerList.add(gridView);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int pos = position + curIndex * pageSize;
                        ToastFactory.makeText(HomePageActivity.this, mDatas.get(pos).getName(), Toast.LENGTH_SHORT).show();
                        ToastFactory.makeText(HomePageActivity.this, "good" + pos, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(HomePageActivity.this, PayActivity.class);
                        intent.putExtra("which_floor", pos);
                        startActivityForResult(intent, 2);
                    }
                });
            }
            //设置适配器
            mPager.setAdapter(new ViewPagerAdapter(mPagerList));
            //设置圆点
            setOvalLayout();
        } catch (NullPointerException e) {
            ToastFactory.makeText(HomePageActivity.this, "当前没有商品", Toast.LENGTH_SHORT).show();
        }

        cabinetDailySalesSharedPreference = getSharedPreferences("cabinet_daily_sales_file", MODE_PRIVATE);
        editor = cabinetDailySalesSharedPreference.edit();   // 使处于可编辑状态

        final String packageName = getPackageName();
        SharedPreferences settings = getSharedPreferences(packageName + "_preferences", MODE_PRIVATE);
        boolean sw = settings.getBoolean("SwitchPreference", false);
        ViewHolder.sw = sw;
        if (sw) {
            ToastFactory.makeText(HomePageActivity.this, "hhh" + packageName, Toast.LENGTH_SHORT).show();
        } else {
            ToastFactory.makeText(HomePageActivity.this, "xxxxxxxxxxxx" + packageName, Toast.LENGTH_SHORT).show();
        }


        //尝试定位操作
        AMapLocationClient mLocationClient = new AMapLocationClient(this);
        mLocationClient.setLocationListener(mLocationListener);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocation(false);
        mLocationOption.setInterval(3000);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();

        context = this;

        textViewCoinMoney = (TextView) findViewById(R.id.coin_money);

        textViewCoinMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences share = getSharedPreferences("serial_mode_file", MODE_PRIVATE);  // 实例化
                SharedPreferences.Editor editor = share.edit();   // 使处于可编辑状态
                editor.putString("serial_mode", "A");
                editor.apply();
            }
        });

        Button a = (Button) findViewById(R.id.a);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences share = getSharedPreferences("serial_mode_file", MODE_PRIVATE);  // 实例化
                SharedPreferences.Editor editor = share.edit();   // 使处于可编辑状态
                editor.putString("serial_mode", "D");
                editor.apply();
                ToastFactory.makeText(HomePageActivity.this, "D", Toast.LENGTH_SHORT).show();
            }
        });

        videoViewHomePageAd = (VideoView) findViewById(R.id.vv_hp_ad);
        imageViewHomePageAd = (ImageView) findViewById(R.id.iv_hp_ad);
        imageViewHomePageAd.setVisibility(View.INVISIBLE);

        share = super.getSharedPreferences(adSettingDataFileName, MODE_PRIVATE);
        if (share.getBoolean("isAdSettingChanged", false)) {
            ToastFactory.makeText(HomePageActivity.this, "good" + share.getString("Frequency_" + 0, "null"), Toast.LENGTH_LONG).show();
        }

        initVideoPath();


        videoViewHomePageAd.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                if (share.getBoolean("isAdSettingChanged", false)) {
                    if (videoListFrequency[videoFileIndex] > 1) {
                        videoListFrequency[videoFileIndex] = videoListFrequency[videoFileIndex] - 1;

                    } else {
                        videoListFrequency[videoFileIndex] = Integer.parseInt(share.getString("Frequency_" + videoFileIndex, "0"));
                        videoFileIndex ++;
                    }
                    File[] currentFiles = FileService.getFiles(TOPATH);

                    if (currentFiles.length != 0) {
                        if (videoFileIndex >= currentFiles.length) {
                            videoFileIndex = 0;
                        }
                        playVideo(currentFiles[videoListOrder[videoFileIndex]].getPath());
                    } else {
                        imageViewHomePageAd.setVisibility(View.VISIBLE);
                        videoViewHomePageAd.setVisibility(View.INVISIBLE);
                    }
                } else {
                    videoFileIndex ++;
                    File[] currentFiles = FileService.getFiles(TOPATH);

                    if (currentFiles.length != 0) {
                        if (videoFileIndex >= currentFiles.length) {
                            videoFileIndex = 0;
                        }
                        playVideo(currentFiles[videoFileIndex].getPath());
                    } else {
                        imageViewHomePageAd.setVisibility(View.VISIBLE);
                        videoViewHomePageAd.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        TextView textViewHomepageTestlogin = (TextView) findViewById(R.id.tv_homepage_testlogin);
        textViewHomepageTestlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePageActivity.this, LoginActivity.class);
                intent.putExtra("IsLogin", true);
                startActivity(intent);
            }
        });

        SendThread sendThread = new SendThread();
        sendThread.start();

    }

    private class SendThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {

//                SharedPreferences preferences = getSharedPreferences("serial_mode_file", MODE_PRIVATE);
//                String serialMode =  preferences.getString("serial_mode", "A");
//                //serialMode = "D";
//                //int whichSerialMode = Integer.parseInt(serialMode);
//                String text = "It is a test message!!";
//                try {
//                    try {
//                        Thread.sleep(200);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    switch (serialMode) {
//                        case "A":
//                            text = "A";
//                            break;
//                        case "D":
//                            text = "D";
//                            break;
//                    }
//
//                    mOutputStream.write(text.getBytes());
//                    mOutputStream.write('\n');
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    private AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    TextView textView = (TextView) findViewById(R.id.tv_homepage_testlogin);
                    textView.setText(aMapLocation.getAddress());
                }
            }
        }
    };

    private void initVideoPath() {
        videoFileIndex = 0;
        try {
            File[] currentFiles = FileService.getFiles(TOPATH);
            if (currentFiles.length != 0) {
                if (share.getBoolean("isAdSettingChanged", false)) {
                    videoListOrder = new int[currentFiles.length];
                    videoListFrequency = new int[currentFiles.length];
                    for (int i = 0; i < currentFiles.length; i++) {
                        videoListFrequency[i] = Integer.parseInt(share.getString("Frequency_" + i, "0"));
                        for (int j = 0; j < currentFiles.length; j++) {
                            if (share.getString("Ad_" + i, "null").equals(currentFiles[j].getName())) {
                                videoListOrder[i] = j;
                                break;
                            }
                        }
                    }
                    playVideo(currentFiles[videoListOrder[videoFileIndex]].getPath());
                }
                else {
                    playVideo(currentFiles[videoFileIndex].getPath());
                }
            } else {
                imageViewHomePageAd.setVisibility(View.VISIBLE);
                videoViewHomePageAd.setVisibility(View.INVISIBLE);
            }
        } catch (NullPointerException e) {
            ToastFactory.makeText(HomePageActivity.this, "找不到文件路径", Toast.LENGTH_SHORT).show();
        }
    }

    public void playVideo(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            videoViewHomePageAd.setVideoPath(file.getPath());
            videoViewHomePageAd.start();
        } else {
            imageViewHomePageAd.setVisibility(View.VISIBLE);
            videoViewHomePageAd.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoViewHomePageAd != null) {
            videoViewHomePageAd.suspend();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_1:
                if (resultCode == RESULT_OK) {
                    videoFileIndex = 0;
                    File[] currentFiles = FileService.getFiles(TOPATH);
                    if (currentFiles.length != 0) {
                        //Toast.makeText(HomePageActivity.this, "good", Toast.LENGTH_LONG).show();
                        imageViewHomePageAd.setVisibility(View.INVISIBLE);
                        videoViewHomePageAd.setVisibility(View.VISIBLE);
                        //videoViewHomePageAd.start();
                        if (share.getBoolean("isAdSettingChanged", false)) {
                            videoListFrequency = new int[currentFiles.length];
                            videoListOrder = new int[currentFiles.length];
                            for (int i = 0; i < currentFiles.length; i++) {
                                videoListFrequency[i] = Integer.parseInt(share.getString("Frequency_" + i, "0"));
                                for (int j = 0; j < currentFiles.length; j++) {
                                    if (share.getString("Ad_" + i, "null").equals(currentFiles[j].getName())) {
                                        videoListOrder[i] = j;
                                        break;
                                    }
                                }
                            }
                            playVideo(currentFiles[videoListOrder[videoFileIndex]].getPath());
                        } else {
                            playVideo(currentFiles[videoFileIndex].getPath());
                        }
                    } else {
                        imageViewHomePageAd.setVisibility(View.VISIBLE);
                        videoViewHomePageAd.setVisibility(View.INVISIBLE);
                    }
                }
                break;
            case REQUEST_PAY_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    final int whichFloor = data.getIntExtra("which_floor", 0);
                    final int whichGoods = data.getIntExtra("which_goods", 0);
                    String text = "You clicked on item" + whichGoods;
                    try {
                        mOutputStream.write(text.getBytes());
                        mOutputStream.write('\n');
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        // 销售信息表记录
                        Sales sales = new Sales();
                        sales.setSales_date(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                        sales.setGoods_id(DataSupport.find(Goods.class, whichGoods).getId());
                        sales.setGoods_name(DataSupport.find(Goods.class, whichGoods).getName());
                        sales.setMachine_floor(whichFloor + 1);//货物层下标0开始，需要加一
                        sales.setTrade_id("5846516");
                        sales.setPay_way("现金");
                        sales.save();

                        // 机柜日销售统计表记录
                        CabinetDailySales cabinetDailySales = new CabinetDailySales();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String cabinetDailySalesDate = sdf.format(new java.util.Date());
                        String date =  cabinetDailySalesSharedPreference.getString(
                                "cabinet_daily_sales_date", "2017-11-14");
                        int num = cabinetDailySalesSharedPreference.getInt(
                                "cabinet_daily_sales_num", 0);
                        float totalMoney = cabinetDailySalesSharedPreference.getFloat("cabinet_daily_sales_total_money", 0);
                        if (cabinetDailySalesDate.equals(date)) {
                            num = num + 1;
                            totalMoney = totalMoney + DataSupport.find(Goods.class, whichGoods).getSales_price();
                            editor.putInt("cabinet_daily_sales_num", num);
                            editor.putFloat("cabinet_daily_sales_total_money", totalMoney);
                            editor.apply();
                            cabinetDailySales.setCabinetDailySalesNum(num);
                            cabinetDailySales.setCabinetDailySalesTotalMoney(totalMoney);
                            cabinetDailySales.updateAll("cabinetDailySalesDate = ?", date);
                        } else {
                            date = cabinetDailySalesDate;
                            num = 1;
                            totalMoney = DataSupport.find(Goods.class, whichGoods).getSales_price();
                            editor.putString("cabinet_daily_sales_date", date);
                            editor.putInt("cabinet_daily_sales_num", num);
                            editor.putFloat("cabinet_daily_sales_total_money", totalMoney);
                            editor.apply();
                            cabinetDailySales.setCabinetDailySalesDate(sdf);
                            cabinetDailySales.setCabinetDailySalesNum(num);
                            cabinetDailySales.setCabinetDailySalesTotalMoney(totalMoney);
                            cabinetDailySales.save();
                        }

                        // 机柜月销售统计表记录
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CabinetMonthlySales cabinetMonthlySales = new CabinetMonthlySales();
                                SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM");
                                String cabinetMonthlySalesDate = mSdf.format(new java.util.Date());
                                String mDate =  cabinetDailySalesSharedPreference.getString(
                                        "cabinet_monthly_sales_date", "2017-11");
                                int mNum = cabinetDailySalesSharedPreference.getInt(
                                        "cabinet_monthly_sales_num", 0);
                                float mTotalMoney = cabinetDailySalesSharedPreference.getFloat(
                                        "cabinet_monthly_sales_total_money", 0);
                                if (cabinetMonthlySalesDate.equals(mDate)) {
                                    mNum = mNum + 1;
                                    mTotalMoney = mTotalMoney + DataSupport.find(Goods.class, whichGoods).getSales_price();
                                    editor.putInt("cabinet_monthly_sales_num", mNum);
                                    editor.putFloat("cabinet_monthly_sales_total_money", mTotalMoney);
                                    editor.apply();
                                    cabinetMonthlySales.setCabinetMonthlySalesNum(mNum);
                                    cabinetMonthlySales.setCabinetMonthlySalesTotalMoney(mTotalMoney);
                                    cabinetMonthlySales.updateAll("cabinetMonthlySalesDate = ?", mDate);
                                } else {
                                    mDate = cabinetMonthlySalesDate;
                                    mNum = 1;
                                    mTotalMoney = DataSupport.find(Goods.class, whichGoods).getSales_price();
                                    editor.putString("cabinet_monthly_sales_date", mDate);
                                    editor.putInt("cabinet_monthly_sales_num", mNum);
                                    editor.putFloat("cabinet_monthly_sales_total_money", mTotalMoney);
                                    editor.apply();
                                    cabinetMonthlySales.setCabinetMonthlySalesDate(mSdf);
                                    cabinetMonthlySales.setCabinetMonthlySalesNum(mNum);
                                    cabinetMonthlySales.setCabinetMonthlySalesTotalMoney(mTotalMoney);
                                    cabinetMonthlySales.save();
                                }
                            }
                        }).start();

                    } catch (NullPointerException e) {
                        ToastFactory.makeText(HomePageActivity.this, "目前没有商品" + whichGoods, Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        handler.postDelayed(runnable, 1000 * SECONDS_OF_AD);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        handler.removeCallbacks(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
        // 用户SECONDS_OF_AD秒没操作了
        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setClass(context, ScreenSaverActivity.class);
        context.startActivity(i);
        }
    };

    public boolean onTouchEvent(android.view.MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: { // 手指下来的时候,取消之前绑定的Runnable

                handler.removeCallbacks(runnable);
                break;
            }
            case MotionEvent.ACTION_UP: { // 手指离开屏幕，发送延迟消息 ，SECONDS_OF_AD秒后执行

                handler.postDelayed(runnable, 1000 * SECONDS_OF_AD);

                break;
            }
        }
        return super.onTouchEvent(event);
    };



    // 串口接收函数
    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            public void run() {
                ToastFactory.makeText(HomePageActivity.this, new String(buffer, 0, size), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 初始化数据源
     */
    private void initDatas() {
        mDatas = new ArrayList<Model>();
        //暂时初始化10个空货物的时候有两个点
        for (int i = 0; i < 10; i++) {
            SharedPreferences preferences = getSharedPreferences("cabinet_floor", MODE_PRIVATE);
            int whichGoods =  preferences.getInt("cabinet_floor_" + i, 0);
            Goods goods = DataSupport.find(Goods.class, whichGoods);
            if (whichGoods == 0) {
                mDatas.add(new Model("", "", R.drawable.ic_category_null));
            } else {
                mDatas.add(new Model("¥ " + String.valueOf(goods.getSales_price()), goods.getName(), goods.getImage_path()));
            }
        }
    }

    /**
     * 设置圆点
     */
    public void setOvalLayout() {
        for (int i = 0; i < pageCount; i++) {
            mLlDot.addView(inflater.inflate(R.layout.dot, null));
        }
        // 默认显示第一页
        mLlDot.getChildAt(0).findViewById(R.id.v_dot)
                .setBackgroundResource(R.drawable.dot_selected);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageSelected(int position) {
                // 取消圆点选中
                mLlDot.getChildAt(curIndex)
                        .findViewById(R.id.v_dot)
                        .setBackgroundResource(R.drawable.dot_normal);
                // 圆点选中
                mLlDot.getChildAt(position)
                        .findViewById(R.id.v_dot)
                        .setBackgroundResource(R.drawable.dot_selected);
                curIndex = position;
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        //finish();
        //handler.postDelayed(runnable, 1000 * SECONDS_OF_AD);
    }
}
