package com.obdasystems.pocmedici.activity;

import android.app.Application;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.obdasystems.pocmedici.R;
import com.obdasystems.pocmedici.adapter.FormListAdapter;
import com.obdasystems.pocmedici.asyncresponse.FinalizedFormAsyncResponse;
import com.obdasystems.pocmedici.asyncresponse.PageQuestionsAsyncResponse;
import com.obdasystems.pocmedici.listener.OnFormRecyclerViewItemClickListener;
import com.obdasystems.pocmedici.persistence.entities.CtcaeFormFillingProcess;
import com.obdasystems.pocmedici.persistence.entities.JoinFormWithMaxPageNumberData;
import com.obdasystems.pocmedici.persistence.repository.CtcaeFormRepository;
import com.obdasystems.pocmedici.persistence.repository.CtcaeGetFinalizedFillingProcessRepository;
import com.obdasystems.pocmedici.persistence.viewmodel.CtcaeFormListViewModel;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class FormListActivity extends AppCompatActivity implements FinalizedFormAsyncResponse {
    private CtcaeFormListViewModel formListViewModel;

    private ArrayList<String> items;
    private ListView listView;

    private boolean firstLoad = true;

    private int counter = 0;
    private CtcaeFormRepository repository;

    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_form_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.form_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_black_24dp);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(ctx, MainActivity.class);
                startActivity(mainIntent);
            }
        });
    }


    protected void onResume() {
        super.onResume();
        RecyclerView recyclerView = findViewById(R.id.formRecyclerView);
        final FormListAdapter adapter = new FormListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager((this)));
        adapter.setOnItemClickListener(new OnFormRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                JoinFormWithMaxPageNumberData clickedForm = adapter.getFormAtPosition(position);
                if(clickedForm!=null) {
                    Intent intent = new Intent(view.getContext(),CtcaeFormActivity.class);
                    intent.putExtra("clickedForm", clickedForm);
                    //startActivityForResult(intent, CTCAE_FORM_SUBMITTED_CODE);
                    startActivity(intent);
                }
            }
        });

        formListViewModel = ViewModelProviders.of(this).get(CtcaeFormListViewModel.class);
        formListViewModel.getAllForms().observe(this, new Observer<List<JoinFormWithMaxPageNumberData>>() {
            @Override
            public void onChanged(@Nullable List<JoinFormWithMaxPageNumberData> ctcaeForms) {
                adapter.setForms(ctcaeForms);
                adapter.notifyDataSetChanged();
            }
        });

        Intent intent = getIntent();
        if(intent!=null) {
            if(intent.hasExtra("fillingProcess") && intent.hasExtra("filledForm")) {
                int fpId = intent.getIntExtra("fillingProcess", -1);
                int fId = intent.getIntExtra("filledForm", -1);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent formlistIntent = new Intent(this, MainActivity.class);
        startActivity(formlistIntent);
    }


    /*****************************
     * ASYNC TASKS CALLBACK
     *****************************/

    @Override
    public void getFinalizedTaskFinished(CtcaeFormFillingProcess finalized) {
        int fpId = finalized.getId();
        int fId = finalized.getFormId();
        GregorianCalendar start = finalized.getStartDate();
        String startStr = getCalendarString(start);
        GregorianCalendar end = finalized.getEndDate();
        String endStr = getCalendarString(end);
        int sentToServer = finalized.getSentToServer();

        Log.i("appMedici","["+this.getClass()+"]finalized fillingProcessId="+fpId+" " +
                "start="+startStr+ " end="+endStr+ " sentToServer="+sentToServer);
    }

    private String getCalendarString(GregorianCalendar cal) {
        int year = cal.get(GregorianCalendar.YEAR);
        int month = cal.get(GregorianCalendar.MONTH);
        int day = cal.get(GregorianCalendar.DAY_OF_YEAR);
        int hour = cal.get(GregorianCalendar.HOUR_OF_DAY);
        int minute = cal.get(GregorianCalendar.MINUTE);
        String dateStr = year+"/"+month+"/"+day+" "+hour+":"+minute;
        return dateStr;
    }

    /*****************************
     * ASYNC TASKS
     *****************************/

    //get the finalized filled form (for logging purposes ONLY)
    private static class GetFinalizedFillingProcessQueryAsyncTask extends AsyncTask<Void, Void, CtcaeFormFillingProcess> {
        private Context ctx;
        private ProgressDialog progDial;
        private int fpId;
        private CtcaeGetFinalizedFillingProcessRepository repository;

        private Application app;
        private FinalizedFormAsyncResponse delegate;

        GetFinalizedFillingProcessQueryAsyncTask( int fillingProcId, Context context, Application app, FinalizedFormAsyncResponse delegate) {
            ctx = context;
            this.fpId = fillingProcId;
            progDial = new ProgressDialog(ctx);
            this.app = app;
            this.delegate = delegate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDial.setMessage("Retrieving just submitted form...");
            progDial.setIndeterminate(false);
            progDial.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDial.setCancelable(false);
            progDial.show();
        }

        @Override
        protected CtcaeFormFillingProcess doInBackground(Void... voids) {
            repository = new CtcaeGetFinalizedFillingProcessRepository(app, fpId);
            CtcaeFormFillingProcess res = repository.getFinalized();
            return res;
        }

        @Override
        protected void onPostExecute(CtcaeFormFillingProcess res) {
            super.onPostExecute(res);
            progDial.dismiss();
            delegate.getFinalizedTaskFinished(res);
        }
    }

}
