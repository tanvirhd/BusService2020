package com.example.busservice2020.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busservice2020.R;
import com.example.busservice2020.interfaces.AdapterCallback;
import com.example.busservice2020.model.AvailableBus;

import java.util.List;

public class AdapterAvailableBus extends RecyclerView.Adapter<AdapterAvailableBus.ViewHoldeAdapterAvailableBus> {
    private static final String TAG="AdapterAvailableBus";
    Context context;
    List<AvailableBus> busList;
    AdapterCallback adapterCallback;

    public AdapterAvailableBus(Context context, List<AvailableBus> busList,AdapterCallback adapterCallback) {
        this.context = context;
        this.busList = busList;
        this.adapterCallback=adapterCallback;
    }

    @NonNull
    @Override
    public ViewHoldeAdapterAvailableBus onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.layout_availablebus_list,parent,false);
        ViewHoldeAdapterAvailableBus viewHolde=new ViewHoldeAdapterAvailableBus(view);
        return viewHolde;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHoldeAdapterAvailableBus holder, int position) {
        //todo complete this part
        final int pos=position;
        holder.busname.setText(busList.get(position).getCompanyname());
        holder.route.setText(busList.get(position).getRoute());
        holder.rem_time_distance.setText(busList.get(pos).getRem_time_distance());

        holder.pickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterCallback.onPickUpRequestCallBack(busList.get(pos).getBusid());
                Log.d(TAG, "onClick: clicked");
            }
        });

        holder.itemHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterCallback.onItemClickCallBack(busList.get(pos).getBusid());
                Log.d(TAG, "onClick: clicked");
            }
        });
    }

    @Override
    public int getItemCount() {
        return busList.size();
    }

    class ViewHoldeAdapterAvailableBus extends RecyclerView.ViewHolder {

        TextView busname,route,rem_time_distance,pickupRequest;
        CardView itemHolder;
        public ViewHoldeAdapterAvailableBus(@NonNull View itemView) {
            super(itemView);
            busname=itemView.findViewById(R.id.busname);
            rem_time_distance=itemView.findViewById(R.id.remainingTimeDistance);
            route=itemView.findViewById(R.id.busRoute);
            pickupRequest=itemView.findViewById(R.id.pickupRequest);
            itemHolder=itemView.findViewById(R.id.itemHolder);
        }
    }
}
