package nivesuresh.traditionallatinmassfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by nivesuresh on 8/31/16.
 */

public class TLMAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context context;
    private List<TLMData> list;

    public TLMAdapter(Context ctx, List<TLMData> objects) {
        context = ctx;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        list = objects;
    }

    @Override
    public int getItemViewType(int position){
        if(position == 0) return 0;
        else return 1;
    }

    @Override
    public int getViewTypeCount(){
        return 2;
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public Object getItem(int position){
        if(list != null){
            return list.get(position);
        }
        return 0;
    }

    @Override
    public int getCount(){
        if(list != null){
            return list.size();
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null){
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder = new ViewHolder();
        } else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TLMData data = (TLMData)getItem(position);
        String churchName = data.getChurchName();

        viewHolder.churchNameTextView = (TextView) convertView.findViewById(R.id.churchNameTextView);
        viewHolder.churchNameTextView.setText(churchName);

        convertView.setTag(viewHolder);
        return convertView;
    }

    public static class ViewHolder{
        TextView churchNameTextView;

        public TextView getChurchNameTextView() {
            return churchNameTextView;
        }
    }
}
