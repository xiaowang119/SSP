package myUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.zhx.ssp.R;

import java.util.List;

public class TableAdapter extends BaseAdapter {
    private List<MyMessage> list;
    private LayoutInflater inflater;

    public TableAdapter(Context context, List<MyMessage> list){
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        int ret = 0;
        if(list!=null){
            ret = list.size();
        }
        return ret;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
        }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyMessage myMessage = (MyMessage) this.getItem(position);
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item, null);
            viewHolder.msgId = (TextView) convertView.findViewById(R.id.message_id);
            viewHolder.msgUnit = (TextView) convertView.findViewById(R.id.message_unit);
            viewHolder.msgRemark = (TextView) convertView.findViewById(R.id.message_remark);
            viewHolder.msgStandard = (TextView) convertView.findViewById(R.id.message_standard);
            viewHolder.msgDetection = (TextView) convertView.findViewById(R.id.message_detection);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.msgId.setText(myMessage.getId());
        viewHolder.msgId.setTextSize(13);
        viewHolder.msgUnit.setText(myMessage.getUnit());
        viewHolder.msgUnit.setTextSize(13);
        viewHolder.msgRemark.setText(myMessage.getRemark());
        viewHolder.msgRemark.setTextSize(13);
        viewHolder.msgStandard.setText(myMessage.getStandard());
        viewHolder.msgStandard.setTextSize(13);
        viewHolder.msgDetection.setText(myMessage.getDetection());
        viewHolder.msgDetection.setTextSize(13);
        return convertView;
    }

    public static class ViewHolder{
        public TextView msgId;
        public TextView msgUnit;
        public TextView msgRemark;
        public TextView msgStandard;
        public TextView msgDetection;
    }

}
