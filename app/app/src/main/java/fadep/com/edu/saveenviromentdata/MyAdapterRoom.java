package fadep.com.edu.saveenviromentdata;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import fadep.com.edu.saveenviromentdata.model.Place;

public class MyAdapterRoom extends RecyclerView.Adapter<MyAdapterRoom.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Place place);
    }

    private final List<Place> mDataset;
    public OnItemClickListener listener;

    public MyAdapterRoom(List<Place> mDataset, OnItemClickListener listener) {
        this.mDataset = mDataset;
        this.listener = listener;
    }

    public void add(Place resp){
        this.mDataset.add(resp);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtNome;
        public TextView txtLat;
        public TextView txtLong;


        public ViewHolder(View v) {
            super(v);
            txtNome = v.findViewById(R.id.txtNome);
            txtLat = v.findViewById(R.id.txtLat);
            txtLong = v.findViewById(R.id.txtLong);
        }

        public void bind(final Place item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public MyAdapterRoom(List<Place> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public MyAdapterRoom.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lista, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.txtNome.setText(mDataset.get(position).getName());
        holder.bind(mDataset.get(position), listener);
        holder.txtLat.setText(String.valueOf(mDataset.get(position).getLat()));
        holder.txtLong.setText(String.valueOf(mDataset.get(position).getLng()));

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}