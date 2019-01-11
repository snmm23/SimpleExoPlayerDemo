package sun.bo.lin.exoplayer.all;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import sun.bo.lin.exoplayer.R;

public class StreamSelectDialog extends Dialog {

    private int selectStreamIndex;
    private ArrayList<StreamGroup> streamGroups;
    private ListView streamList;
    private StreamSelectListener listener;

    public StreamSelectDialog(@NonNull Context context, int selectStreamIndex, ArrayList<StreamGroup> streamGroups, StreamSelectListener listener) {
        super(context, R.style.Dialog);
        this.selectStreamIndex = selectStreamIndex;
        this.streamGroups = streamGroups;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stream_select_dialog_layout);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();

        int max;
        int min;
        if(displayMetrics.widthPixels > displayMetrics.heightPixels){
            max = displayMetrics.widthPixels;
            min = displayMetrics.heightPixels;
        } else {
            max = displayMetrics.heightPixels;
            min = displayMetrics.widthPixels;
        }

        layoutParams.width = (int) (max * 0.35);
        layoutParams.height = min;
        layoutParams.gravity = Gravity.RIGHT;
        onWindowAttributesChanged(layoutParams);

        initViews();
    }

    private void initViews(){
        streamList = findViewById(R.id.streamList);
        streamList.setAdapter(new StreamSelectAdapter());
    }

    private class StreamSelectAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if(streamGroups != null && streamGroups.size() > 0){
                return streamGroups.size();
            }
            return 0;
        }

        @Override
        public StreamGroup getItem(int position) {
            return streamGroups.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.stream_select_adapter_item_layout, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.streamName = convertView.findViewById(R.id.streamName);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.streamName.setText(getItem(position).getName());
            viewHolder.streamName.setTextColor(position == selectStreamIndex ?
                    getContext().getResources().getColor(R.color.color_FFDF5159) :
                    getContext().getResources().getColor(android.R.color.white));
            viewHolder.streamName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(position != selectStreamIndex){
                        listener.onSwitchStream(position);
                    }
                    dismiss();
                }
            });
            return convertView;
        }

        private class ViewHolder {
            TextView streamName;
        }
    }

    public interface StreamSelectListener {
        void onSwitchStream(int selectStreamIndex);
    }
}
