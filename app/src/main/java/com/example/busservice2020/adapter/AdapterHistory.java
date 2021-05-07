package com.example.busservice2020.adapter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busservice2020.R;
import com.example.busservice2020.model.ModelHistory;
import com.example.busservice2020.model.ModelHistoryRyc;
import java.util.List;


public class AdapterHistory extends RecyclerView.Adapter<AdapterHistory.AdapterHistoryViewHolder> {

    Context context;
    List<ModelHistoryRyc> historyList;

    public AdapterHistory(Context context, List<ModelHistoryRyc> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public AdapterHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.layout_history,parent,false);
        AdapterHistoryViewHolder adapterHistoryViewHolder=new AdapterHistoryViewHolder(view);
        return adapterHistoryViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterHistoryViewHolder holder, int position) {
         holder.busname.setText(historyList.get(position).getBusname());
        holder.buslicense.setText(historyList.get(position).getBuslicense());
        holder.date.setText(historyList.get(position).getDate());
        holder.pickuplicationname.setText("Pickup: "+historyList.get(position).getPickuplocationname());
        holder.deslictationname.setText("Destination: "+historyList.get(position).getDestinationPlaceName());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    class AdapterHistoryViewHolder extends RecyclerView.ViewHolder{
        TextView busname,buslicense,date,pickuplicationname,deslictationname;
        public AdapterHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            busname=itemView.findViewById(R.id.dbusname);
            buslicense=itemView.findViewById(R.id.dbuslicense);
            date=itemView.findViewById(R.id.dtimedate);
            pickuplicationname=itemView.findViewById(R.id.dpickuplocationname);
            deslictationname=itemView.findViewById(R.id.ddestinationlocationname);
        }
    }
}
