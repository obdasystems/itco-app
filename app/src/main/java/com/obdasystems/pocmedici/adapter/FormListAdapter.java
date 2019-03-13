package com.obdasystems.pocmedici.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.obdasystems.pocmedici.R;
import com.obdasystems.pocmedici.listener.OnFormRecyclerViewItemClickListener;
import com.obdasystems.pocmedici.persistence.entities.CtcaeForm;
import com.obdasystems.pocmedici.persistence.entities.JoinFormWithMaxPageNumberData;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class FormListAdapter extends RecyclerView.Adapter<FormListAdapter.FormViewHolder> {

    private final LayoutInflater inflater;
    private List<JoinFormWithMaxPageNumberData> forms;

    OnFormRecyclerViewItemClickListener mClickListener;

    public FormListAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public FormViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View itemView = inflater.inflate(R.layout.form_reciclerview_item, parent, false);
        View itemView = inflater.inflate(R.layout.form_recyclerview_row_layout, parent, false);
        return new FormViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FormViewHolder holder, int position) {
        if(forms!=null && forms.size()>0) {
            JoinFormWithMaxPageNumberData current = forms.get(position);
            holder.formImageView.setImageResource(R.drawable.ic_description_black_24dp);
            holder.formTitleView.setText(current.getFormTitle() + "[class="+current.getFormClass()+", per="+current.getFormPeriodicity()
                    + " pages="+current.getLastPageNumber()+"]");
            holder.formDescriptionView.setText(current.getFormInstructions());

            //holder.formTitleItemView.setText(current.getId() + " " + current.getFormClass() + " " + current.getFormPeriodicity());
        }
        else {
            holder.formImageView.setImageResource(R.drawable.ic_add_alarm_black_24dp);
            holder.formTitleView.setText("NO FORMS AVAILABLE!!");
            //holder.formTitleItemView.setText("No form found!!");
        }
    }

    @Override
    public int getItemCount() {
        if(forms != null) {
            return forms.size();
        }
        return 1;
    }


    //CUSTOM METHODS
    public void setForms(List<JoinFormWithMaxPageNumberData> forms) {
        this.forms = forms;
    }

    public JoinFormWithMaxPageNumberData getFormAtPosition(int position) {
        if(this.forms!=null) {
            return this.forms.get(position);
        }
        return null;
    }

    public void setOnItemClickListener(final OnFormRecyclerViewItemClickListener listener) {
        this.mClickListener = listener;
    }


    class FormViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //private final TextView formTitleItemView;
        private final CardView formCardView;
        private final TextView formTitleView;
        private final TextView formDescriptionView;
        private final ImageView formImageView;

        private FormViewHolder(View itemView) {
            super(itemView);
            formCardView = itemView.findViewById(R.id.formCardView);
            formTitleView = itemView.findViewById(R.id.formTitleTextView);
            formDescriptionView = itemView.findViewById(R.id.formDescriptionTextView);
            formImageView = itemView.findViewById(R.id.formImageView);
            itemView.setOnClickListener(this);
            //formTitleItemView = itemView.findViewById(R.id.formTextView);
        }

        @Override
        public void onClick(View v) {
            if(mClickListener!=null) {
                mClickListener.onItemClick(v, getLayoutPosition());
            }
        }
    }
}
