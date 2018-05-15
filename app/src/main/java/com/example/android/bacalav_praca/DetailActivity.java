package com.example.android.bacalav_praca;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;

public class DetailActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{


    private GraphView mGraph;
    private TextView mResult;
    private TextView mMinTime;
    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    private LineGraphSeries<DataPoint> series_Ck;
    private LineGraphSeries<DataPoint> series_Ct;
    public static ArrayList<Integer> xDataArr;
    public static ArrayList<Double> ckArr;
    public static ArrayList<Double> ctArr;
    public static ArrayList<Double> ctMaxArr;
    private static int FLOATING_POINT = 2;
    private static int mPeriod = 0;
    private static int mDays = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar !=null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mGraph = (GraphView) this.findViewById(R.id.graph_view);
        mResult = (TextView) this.findViewById(R.id.result);
        mMinTime = (TextView) this.findViewById(R.id.min_time) ;
        String mAmount = "";
        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
                mAmount = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);
            }
        }
        int amount = 0;
        try {
             amount = Integer.parseInt(mAmount);
        } catch (NumberFormatException ex) {
            Log.e(LOG_TAG, "Failed to parse text to number: " + ex.getMessage());
        }
        setupSharedPreferences();
        dataGeneration(amount);
    }


    private void setupSharedPreferences() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        loadCasFromPreferences(sharedPreferences);
        loadPeriodaFromPreferences(sharedPreferences);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void loadCasFromPreferences(SharedPreferences sharedPreferences) {
        mDays = Integer.parseInt(sharedPreferences.getString(getString(R.string.key_cas_size),getString(R.string.defult_cas_size)));
    }

    private void loadPeriodaFromPreferences(SharedPreferences sharedPreferences) {
        mPeriod = Integer.parseInt(sharedPreferences.getString(getString(R.string.key_period_size),getString(R.string.pref_value_8)));
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         if (key.equals(getString(R.string.key_period_size))) {
            loadPeriodaFromPreferences(sharedPreferences);
        } else if (key.equals(getString(R.string.key_cas_size))) {
            mDays = Integer.parseInt(sharedPreferences.getString(getString(R.string.key_cas_size), "1.0"));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home)
            NavUtils.navigateUpFromSameTask(this);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void dataGeneration(int m)
    {
        double aI= (double) m;
        double cI = aI/4.5;
        int t = 0;
        int period = mPeriod;
        int cas = 24*mDays + 1;
        xDataArr = new ArrayList();
        ctMaxArr = new ArrayList();
        ckArr = new ArrayList();
        ctArr = new ArrayList();
        //=============Resienie na intervale <0;T> (bez zacatocnych podmienok)==============
        do{
            xDataArr.add(t);
            ckArr.add(Vypocty.get_cK0(cI,t));
            ctArr.add(Vypocty.get_cT0(cI,t));
            t++;
        }while(t<=period);
        //=============Resienie na intervale <nT;(n+1)T> (zo zacatocnymi podmienkami)==============
        int n =1;
        for(int j = t + (period - 1); j < cas; j+=period){
            double ctMax = 0;
            do{
                xDataArr.add(t);
                ckArr.add(Vypocty.get_cK_podmienka(cI,t, period,n));
                ctArr.add(Vypocty.get_cT_podmienka(cI, t, period,n));

                if(ctMax < Vypocty.get_cT_podmienka(cI, t, period,n)){
                    ctMax = Vypocty.get_cT_podmienka(cI, t, period,n);
                }
                t++;
            }while(t-1<j);
            n++;
            ctMaxArr.add(ctMax);
        }
        int z = t;
        do{
            xDataArr.add(t);
            ckArr.add(Vypocty.get_cK_podmienka(cI,t, period,n));
            ctArr.add(Vypocty.get_cT_podmienka(cI, t, period,n));
            t++;
        }while(t<=z+100);

        paint();
        int minCas = ctMaxTime();
        double konecna_koncentracia = Vypocty.getStrHodnota(cI, minCas, n, period);
        int mCas = minCas*period;
        mResult.setText(Math.floor(konecna_koncentracia) + " mg" );
        mMinTime.setText(mCas + " hodin");
    }

    public void paint()
    {
        series_Ck = new LineGraphSeries<DataPoint>();
        series_Ct = new LineGraphSeries<DataPoint>();
        series_Ct.setColor(Color.RED);
        series_Ck.setColor(Color.BLUE);
        double[] ckData = new double[ckArr.size()];
        for(int j = 0; j < ckArr.size(); j++) ckData[j] = ckArr.get(j);

        double[] ctData = new double[ctArr.size()];
        for(int j = 0; j < ctArr.size(); j++) ctData[j] = ctArr.get(j);

        double[] xData = new double[xDataArr.size()];
        for(int j = 0; j < xDataArr.size(); j++) xData[j] = xDataArr.get(j);

        for(int j = 0; j < xDataArr.size(); j++){
            series_Ck.appendData(new DataPoint(xData[j], ckData[j]),true, 3*xDataArr.size());
            series_Ct.appendData(new DataPoint(xData[j], ctData[j]),true, 3*xDataArr.size());
        }
        GridLabelRenderer gridLabel = mGraph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("hours");
        mGraph.getViewport().setXAxisBoundsManual(true);
        mGraph.getViewport().setYAxisBoundsManual(true);
        mGraph.getViewport().setMinY(0);
        mGraph.getViewport().setMaxY(180);
        series_Ck.setTitle("Ck(t)");
        series_Ct.setTitle("Ct(t)");
        mGraph.getLegendRenderer().setVisible(true);
        mGraph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        mGraph.getViewport().setMinX(0);
        mGraph.getViewport().setMaxX(200);
        mGraph.getViewport().setScrollable(true);
        mGraph.getViewport().setScrollableY(true);
        mGraph.getViewport().setScalableY(true);
        mGraph.getViewport().setScalable(true);
        mGraph.addSeries(series_Ck);
        mGraph.addSeries(series_Ct);
    }

    public static int ctMaxTime(){

        int counter = 0;
        int median = (int)Math.floor(ctMaxArr.get(ctMaxArr.size()-1)) - FLOATING_POINT;
        for(int i =0; i< ctMaxArr.size();i++)
        {
            if(ctMaxArr.get(i) > median)
            {
                counter = i;
                break;
            }
        }

        return (counter+1);
    }



}
