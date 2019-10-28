package MyUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zhx.ssp.R;

import java.util.List;

// 自定义的 Adapter ， 重写 getCount 、 getItem 、 getItemId 和 getView 方法。其中的 getView 方法最为重要
public class DeviceListAdapter extends ArrayAdapter<DeviceListItem> {
    // 声明一个 LayoutFlater 对象
    private LayoutInflater inflater ;
    private Context ctx ;
    // 声明一个 List 对象 ， 其元素的数据类型为 ListItemData 。因此这个 list 对象实际上
    // 就是 ListView 对象的数据。
    private final List<DeviceListItem> list ;

    public DeviceListAdapter(Context ctx, List<DeviceListItem> list)
    {
        super (ctx, R.layout.sensor_list_item, list);
        this . ctx =ctx;
        this . list =list;
        inflater =(LayoutInflater)ctx.getSystemService(Context. LAYOUT_INFLATER_SERVICE );
    }

    public int getCount()
    {
        return list .size();
    }

    public DeviceListItem getItem( int position)
    {
        return list .get(position);
    }

    public long getItemId( int position)
    {
        return position;
    }

    // 返回一个 RelativeLayout 对象 ， 其中包括一个 ImageView 、一个 TextView 以及一个 CheckBox
    public View getView( int position, View convertView, ViewGroup parent)
    {
        //getView 方法中的第二个参数 convertView 有时候可能会是 null ， 在这样的情况下 ，
        // 我们就必须创建一个新的 rowView(ListView 中每一个条目需要用到的 ) 。但是，如果
        //convertView 不为 null 的时候，它是什么呢？它实际上就是前面通过 inflate 方法
        // 得到的 rowView( 见下面代码 ) 。这种情况主要发生在 ListView 滚动的时候：当一个
        // 新的条目 ( 行 ) 出现的时候， Android 首先会试图重复使用被移除屏幕的那些条目所
        // 对应的 rowView 对象。由于每一行都有相同的结构，因此可以通过 findViewById 方法
        // 得到 rowView 中各个对象，根据相关的数据改变这些对象，然后将 contentView 对象
        // 返回，而不需要重新构建一个 rowView 对象。

        // 所以，在这里，我们先检查 convertView 是否为 null ，如果是 null 的，那么我们创建
        // 一个新的 rowView ，否则，我们重用 convertView 。这样做可以大大减少耗时和耗资源
        // 的 inflate 的调用。根据 2010 年 GoogleI/O 大会，这样做比每次都 inflate 的做法的
        // 性能快出 150% ，如果 rowView 包含的对象很复杂的话，快出 150% 也许都是低估了。

        // 另外， 这样做，还可以节省内存。如果如下面重复利用业已存在的 rowView ，那么
        // 仅需要 6 个 rowView 对象即可 ( 假定屏幕可以显示的行数是 6) ，假定每个 rowView 所占用的
        // 内存是 6kB( 有图像的时候，超过这个数字很容易 ) ，那么一共需要的内存是 36kB 。如果不
        // 采取这种重复利用的方式，在假定有 1000 行，那么所需要的内存就是 6MB 了，而且所需要
        // 的内存和 ListView 中的行数有关，这本身也不符合可扩展性的原则，容易造成性能上
        // 的不稳定。

        final int pos= position;

        View rowView = (View)convertView;

        if (rowView== null )
        {
            rowView= (View) inflater .inflate(R.layout.sensor_list_item , null , true );
        }

// 获得 ImageView 对象
        ImageView iv = (ImageView)rowView.findViewById(R.id.meter_image );
        // 指定对应 position 的 Image
        iv.setImageResource( list .get(pos).getDrawable_id());

        // 获得 TextView 对象
        TextView tv = (TextView)rowView.findViewById(R.id.sensor_list_text );
        // 指定对应 position 的 Text
        tv.setText( list .get(pos).getName_id());
        // 设定文字颜色
        /*if (position%2== 0) {
            tv.setTextColor(Color. YELLOW );
        } else {
            tv.setTextColor(Color. GREEN );
        }*/
        // 为 TextView 对象增加一个 Tag ， 以便在后续的处理中 ， 可以通过
        //findViewWithTag 方法来获取这个 TextView 对象，注意 setTag 的参数可以是任意对象
        tv.setTag( "tagTextView" );

        /*// 获得 CheckBox 对象
        CheckBox chkbox = (CheckBox)rowView.findViewById(R.id. row_checkbox );
        // 为 CheckBox 对象增加一个 Tag ， 以便在后续的处理中 ， 可以通过
        //findViewWithTag 方法来获取这个 TextView 对象，注意 setTag 的参数可以是任意对象
        chkbox.setTag( "tagCheckBox" );
        // 为 CheckBox 设定 CheckedChangedListener
        chkbox.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                // 如果有 CheckBox 被点击了 ( 有可能是由 unchecked 变为 checked ， 也有可能是由 checked 变为 unchecked) ，
                // 那么，我们在 list 中保存对应位置上的 CheckBox 的状态
                list .get(pos).setSelected(isChecked);
                String checkedItems = "The following items are checked:/n/n" ;

                int j= 0;     // 一个标记
                // 根据 list 中记录的状态 ， 输出 ListView 中对应 CheckBox 状态为 checked 的条目
                for ( int i= 0; i < list .size(); ++i)
                {
                    if ( list .get(i).isSelected())
                    {
                        // 通过 getString 方法 (Context 中定义的 ) 获取 id 对应的字符串
                        checkedItems+= i + "/t" + ctx .getString( list .get(i).getOs_id())+ "/n" ;
                        ++j;
                    }
                }
                if (j== 0)
                {
                    checkedItems+= "NO ITEM CHECKED." ;
                }
                Toast.makeText ( ctx ,checkedItems, Toast. LENGTH_SHORT ).show();
            }
        });
        // 下面这行特别重要 ， 否则 ListView 中的 CheckBox 不能正常显示。
        chkbox.setChecked( list .get(pos).isSelected());*/
        return rowView;
    }
}