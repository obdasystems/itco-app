package com.obdasystems.pocmedici.adapter;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.obdasystems.pocmedici.R;
import com.obdasystems.pocmedici.persistence.entities.CtcaeFormQuestion;
import com.obdasystems.pocmedici.persistence.entities.CtcaeFormQuestionAnswered;
import com.obdasystems.pocmedici.persistence.entities.JoinFormPageQuestionsWithPossibleAnswerData;
import com.obdasystems.pocmedici.persistence.repository.CtcaeFillingProcessAnsweredQuestionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormQuestionListAdapter
        extends RecyclerView.Adapter<FormQuestionListAdapter.MyViewHolder> {
    private Context mContext;
    private List<CtcaeFormQuestion> questions;
    private List<JoinFormPageQuestionsWithPossibleAnswerData> questionsWithAnswers;
    private List<CtcaeFormQuestionAnswered> answeredQuestions;
    private Application application;
    private int fillingProcessId;
    private int formId;

    private SparseBooleanArray selectedItems;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;

    // index is used to animate only the selected row
    // dirty fix, find a better solution
    private static int currentSelectedIndex = -1;

    public FormQuestionListAdapter(List<CtcaeFormQuestion> questions, List<JoinFormPageQuestionsWithPossibleAnswerData> questionsWithAnswers,
                                   List<CtcaeFormQuestionAnswered> answeredQuestions, int fillingProcessId, int formId, Application app, Context context) {
        this.questions = questions;
        this.questionsWithAnswers = questionsWithAnswers;
        this.answeredQuestions = answeredQuestions;
        this.mContext = context;
        this.fillingProcessId = fillingProcessId;
        this.application = app;
        this.formId = formId;
    }

    @Override
    public int getItemCount() {
        if(questions != null) {
            return questions.size();
        }
        return 0;
    }

    @Override
    public FormQuestionListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.question_list_row, parent, false);

        return new FormQuestionListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final FormQuestionListAdapter.MyViewHolder holder, final int position) {
        holder.possAnswRadioGroupView.removeAllViews();

        CtcaeFormQuestion currQuestion = questions.get(position);
        int currQuestionId = currQuestion.getId();
        holder.formQuestionTextView.setText(currQuestion.getText());

        holder.pageId = currQuestion.getPageId();
        holder.questionId = currQuestionId;

        RadioGroup.LayoutParams rprms;
        int baseRbId = (int) System.currentTimeMillis(); //(position+1)*100;
        int counter = 0;
        int checkedRbId = -1;
        boolean foundChecked = false;
        Log.i("appMedici","["+this.getClass()+"]found "+answeredQuestions.size()+" " +
                " answered questions");
        for(JoinFormPageQuestionsWithPossibleAnswerData join:questionsWithAnswers) {
            if(join.getQuestionId()==currQuestionId) {
                RadioButton rb = new RadioButton(FormQuestionListAdapter.this.mContext);
                int rbId = baseRbId + counter++;
                rb.setId(rbId);
                rb.setText(join.getPossibleAnswerText());
                rb.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                holder.possAnswRadioGroupView.addView(rb);
                holder.buttonLabelToAnswerIdMap.put(join.getPossibleAnswerText(), join.getPossibleAnswerId());
                holder.buttonIdToLabel.put(rbId, join.getPossibleAnswerText());
                for(CtcaeFormQuestionAnswered answered:answeredQuestions) {
                    if(answered.getQuestionId()==join.getQuestionId() && answered.getAnswerId()==join.getPossibleAnswerId()) {
                        checkedRbId = rbId;
                        foundChecked = true;
                        holder.previouslyCheckedRbId = rbId;
                        holder.lastChecked = rb;
                        rb.setChecked(true);
                        break;
                    }
                }
            }
        }
        if(foundChecked) {
            holder.possAnswRadioGroupView.check(checkedRbId);
        }

    }

    @Override
    public long getItemId(int position) {
        return questions.get(position).getId();
    }

    //CUSTOM METHODS
    public void setQuestions(List<CtcaeFormQuestion> questions) {
        this.questions = questions;
    }

    public void setQuestionsWithAnswers(List<JoinFormPageQuestionsWithPossibleAnswerData> questionsWithAnswers) {
        this.questionsWithAnswers = questionsWithAnswers;
    }

    public void setAlreadyAnsweredQuestions(List<CtcaeFormQuestionAnswered> answeredQuestions) {
        this.answeredQuestions = answeredQuestions;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView formQuestionTextView;
        private final RadioGroup possAnswRadioGroupView;

        private Map<String, Integer> buttonLabelToAnswerIdMap;
        private int pageId;
        private int questionId;
        private int previouslyCheckedRbId = -1;

        private RadioButton lastChecked;

        //A REGIME DA USARE SOLO CON ANDROID VERSIONS VECCHIE
        private Map<Integer, String> buttonIdToLabel;

        private MyViewHolder(View itemView) {
            super(itemView);
            buttonLabelToAnswerIdMap = new HashMap<>();
            buttonIdToLabel = new HashMap<>();
            formQuestionTextView = itemView.findViewById(R.id.questionText);
            possAnswRadioGroupView = itemView.findViewById(R.id.questionAnswersRadioGroup);
            possAnswRadioGroupView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId != previouslyCheckedRbId) {
                        int btCount = group.getChildCount();

                        //group.clearCheck();

                        /*for(int i=0;i<btCount;i++) {
                            RadioButton child = (RadioButton)group.getChildAt(i);
                            int childId = child.getId();
                            if(childId==checkedId) {
                                child.setChecked(true);
                            }
                        }*/

                        if(lastChecked!=null) {
                            lastChecked.setChecked(false);
                        }
                        for(int i=0;i<btCount;i++) {
                            RadioButton child = (RadioButton)group.getChildAt(i);
                            int childId = child.getId();
                            if(childId==checkedId) {
                                lastChecked = child;
                            }
                        }

                        //group.check(checkedId);

                        /*possAnswRadioGroupView.clearCheck();
                        group.check(checkedId);*/

                        RadioButton checked = itemView.findViewById(checkedId);
                        String checkedText;
                        if(checked != null) {
                            checkedText = (String) checked.getText();
                        }
                        else {
                            checkedText = buttonIdToLabel.get(checkedId);
                        }
                        if (!buttonLabelToAnswerIdMap.isEmpty()) {
                            if (buttonLabelToAnswerIdMap.keySet().contains(checkedText)) {
                                int currAnswId = buttonLabelToAnswerIdMap.get(checkedText);
                                FormQuestionListAdapter.QueryAsyncTask task = new FormQuestionListAdapter.QueryAsyncTask(fillingProcessId, formId, pageId, questionId, currAnswId, mContext, application);
                                task.execute();
                                String msg = "You've just selected answer with id= " + currAnswId;
                                //Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mContext, "You've just selected " + checkedText, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(mContext, "You've just selected " + checkedText, Toast.LENGTH_LONG).show();
                        }

                    }
                }
            });
        }

    }

    private static class QueryAsyncTask extends AsyncTask<Void, Void, Void> {
        //private ProgressDialog progDial;
        private CtcaeFillingProcessAnsweredQuestionRepository repository;
        private int pageId;
        private int questionId;
        private int answerId;
        private Context ctx;
        private Application app;
        private int fillingProcessId;
        private int formId;

        QueryAsyncTask(int fillProcId, int formId, int pageId, int questionId, int answerId, Context context, Application app) {
            this.fillingProcessId = fillProcId;
            this.formId = formId;
            this.pageId = pageId;
            this.questionId = questionId;
            this.answerId = answerId;
            this.ctx = context;
            //progDial = new ProgressDialog(ctx);
            this.app = app;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*progDial.setMessage("Saving answer...");
            progDial.setIndeterminate(false);
            progDial.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDial.setCancelable(false);
            progDial.show();*/
        }

        @Override
        protected Void doInBackground(Void... voids) {
            repository = new CtcaeFillingProcessAnsweredQuestionRepository(app, fillingProcessId, formId);
            repository.insertAnsweredQuestion(pageId, questionId, answerId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //progDial.dismiss();
        }
    }
}
