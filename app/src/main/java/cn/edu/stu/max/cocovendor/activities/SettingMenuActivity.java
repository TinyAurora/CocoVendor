package cn.edu.stu.max.cocovendor.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import cn.edu.stu.max.cocovendor.R;

public class SettingMenuActivity extends AppCompatActivity {

    private ButtonListener buttonListener = new ButtonListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button buttonSystemSetting = (Button) findViewById(R.id.btn_system_setting);
        buttonSystemSetting.setOnClickListener(buttonListener);
        Button buttonSheetSetting = (Button) findViewById(R.id.btn_sheet_setting);
        buttonSheetSetting.setOnClickListener(buttonListener);
        Button buttonGoodsTest = (Button) findViewById(R.id.btn_goods_test);
        buttonGoodsTest.setOnClickListener(buttonListener);
        Button buttonAdSetting = (Button) findViewById(R.id.btn_ad_setting);
        buttonAdSetting.setOnClickListener(buttonListener);
        Button buttonMoneySetting = (Button) findViewById(R.id.btn_money_setting);
        buttonMoneySetting.setOnClickListener(buttonListener);
        Button buttonHelp = (Button) findViewById(R.id.btn_help);
        buttonHelp.setOnClickListener(buttonListener);
        Button buttonSalesSetting = (Button) findViewById(R.id.btn_sales_setting);
        buttonSalesSetting.setOnClickListener(buttonListener);
        Button buttonSalesAnalyze = (Button) findViewById(R.id.btn_sales_analyze);
        buttonSalesAnalyze.setOnClickListener(buttonListener);
        Button buttonReturnSales = (Button) findViewById(R.id.btn_setting_return_sales);
        buttonReturnSales.setOnClickListener(buttonListener);
        Button buttonReboot = (Button) findViewById(R.id.btn_reboot);
        buttonReboot.setOnClickListener(buttonListener);
    }

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_system_setting:
                    Intent intent_sys_setting = new Intent(SettingMenuActivity.this, SystemSettingActivity.class);
                    startActivity(intent_sys_setting);
                    break;
                case R.id.btn_sheet_setting:
                    Intent intent_sheet_setting = new Intent(SettingMenuActivity.this, SheetActivity.class);
                    startActivity(intent_sheet_setting);
                    break;
                case R.id.btn_goods_test:
                    break;
                case R.id.btn_ad_setting:
                    Intent intent_ad_setting = new Intent(SettingMenuActivity.this, AdSettingActivity.class);
                    startActivity(intent_ad_setting);
                    break;
                case R.id.btn_money_setting:
                    break;
                case R.id.btn_help:
                    break;
                case R.id.btn_sales_setting:
                    Intent intent_sales_setting = new Intent(SettingMenuActivity.this, SalesSettingActivity.class);
                    startActivity(intent_sales_setting);
                    break;
                case R.id.btn_sales_analyze:
                    Intent intent_sales_analyze = new Intent(SettingMenuActivity.this, SalesAnalyzeActivity.class);
                    startActivity(intent_sales_analyze);
                    break;
                case R.id.btn_setting_return_sales:
                    Intent intent_homepage = new Intent(SettingMenuActivity.this, HomePageActivity.class);
                    startActivity(intent_homepage);
                    break;
                case R.id.btn_reboot:
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
