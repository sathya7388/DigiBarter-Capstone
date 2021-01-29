package com.example.digibarter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class filterPage extends AppCompatActivity {
    ChipGroup timeChipGroup;
    Chip all;
    Chip last1;
    Chip last7;
    Chip last30;
    SeekBar distanceSlider;
    TextView distanceCount;
    Spinner category;
    Button reset;
    Button applyFilter;
    RadioGroup adPostedRadioGroup;
    RadioButton user;
    RadioButton partner;
    RadioButton both;
    String categoryValue = "0", userTypeValue = "-1", timeValue = "all";
    int distanceFilter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_page);
        ArrayList<String> categoryList = getIntent().getStringArrayListExtra("categorySpinner");
        categoryValue = getIntent().getStringExtra("categoryValue");
        userTypeValue = getIntent().getStringExtra("userTypeValue");
        timeValue = getIntent().getStringExtra("timeValue");
        distanceFilter = Integer.parseInt(getIntent().getStringExtra("distanceFilter"));

        adPostedRadioGroup = findViewById(R.id.adPosted);
        user = findViewById(R.id.user);
        partner = findViewById(R.id.partner);
        both = findViewById(R.id.both);


        timeChipGroup = findViewById(R.id.timeChipGroup);
        all = findViewById(R.id.all);
        last1 = findViewById(R.id.last1);
        last7 = findViewById(R.id.last7);
        last30 = findViewById(R.id.last30);

        distanceSlider = findViewById(R.id.distanceSlider);
        distanceCount = findViewById(R.id.distanceCount);
        category = findViewById(R.id.category);

        reset = findViewById(R.id.reset);
        applyFilter = findViewById(R.id.applyFilter);


        reset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                category.setSelection(0);
                distanceCount.setText("Default");
                distanceSlider.setProgress(1000);
                adPostedRadioGroup.check(R.id.both);
                timeChipGroup.check(R.id.all);

                categoryValue = "1";
                userTypeValue = "-1";
                timeValue = "all";
                distanceFilter = 1000;
            }
        });
        applyFilter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (userTypeValue.equals("2")) {
                    intent.putExtra("userTypeValue", "-1");
                    userTypeValue = "-1";
                } else {
                    intent.putExtra("userTypeValue", userTypeValue);
                }
                if (categoryValue.equals("0")) {
                    intent.putExtra("categoryValue", categoryValue);
                    categoryValue = "1";
                } else {
                    intent.putExtra("categoryValue", categoryValue);
                }

                intent.putExtra("timeValue", timeValue);
                intent.putExtra("distanceFilter", String.valueOf(distanceFilter));
                String data = "https://digi-barter.herokuapp.com/getProducts?userType=" + userTypeValue + "&category=" + categoryValue + "&time=" + timeValue;
                intent.putExtra("filterQuery", data);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        ArrayAdapter categoryAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(categoryAdapter);


        //setting Default values
        if(Integer.parseInt(categoryValue)>0){
            category.setSelection(Integer.parseInt(categoryValue)-1);
        }else{
            category.setSelection(Integer.parseInt(categoryValue));
        }

        adPostedRadioGroup.check(Integer.parseInt(userTypeValue));
        if (userTypeValue.equals("-1")) {
            adPostedRadioGroup.check(R.id.both);
        } else {
            adPostedRadioGroup.check(adPostedRadioGroup.getChildAt(Integer.parseInt(userTypeValue)).getId());
        }


        distanceSlider.setProgress(distanceFilter);

        if (distanceFilter == 1000) {
            distanceCount.setText("Default");
        } else {
            distanceCount.setText(distanceFilter + " km");
        }

        switch (timeValue) {
            case "all":
                timeChipGroup.check(R.id.all);
                break;
            case "last1":
                timeChipGroup.check(R.id.last1);
                break;
            case "last7":
                timeChipGroup.check(R.id.last7);
                break;
            case "last30":
                timeChipGroup.check(R.id.last30);
                break;
        }

        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryValue = String.valueOf(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adPostedRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.user:
                        userTypeValue = "0";
                        break;
                    case R.id.partner:
                        userTypeValue = "1";
                        break;
                    case R.id.both:
                        userTypeValue = "-1";
                        break;
                }
            }
        });

        distanceSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 100) {
                    progress = ((int) Math.round(progress / 10)) * 10;
                } else if (progress < 500) {
                    progress = ((int) Math.round(progress / 25)) * 25;
                } else if (progress < 750) {
                    progress = ((int) Math.round(progress / 50)) * 50;
                } else if (progress > 500) {
                    progress = ((int) Math.round(progress / 100)) * 100;
                }

                seekBar.setProgress(progress);
                distanceCount.setText(progress + " km");
                distanceFilter = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //write custom code to on start progress
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (distanceFilter == 1000) {
                    distanceCount.setText("Default");
                } else {
                    distanceCount.setText(distanceFilter + " km");
                }
            }
        });

        timeChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.all:
                        timeValue = "all";
                        break;
                    case R.id.last1:
                        timeValue = "last1";
                        break;
                    case R.id.last7:
                        timeValue = "last7";
                        break;
                    case R.id.last30:
                        timeValue = "last30";
                        break;
                }
            }
        });

    }
}