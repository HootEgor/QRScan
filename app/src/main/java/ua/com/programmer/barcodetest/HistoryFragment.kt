package ua.com.programmer.barcodetest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.mlkit.vision.barcode.common.Barcode;

public class HistoryFragment extends Fragment {

    private View mFragmentView;
    private RecyclerView mRecyclerView;
    private HistoryItemAdapter mItemAdapter;
    private final Utils utils = new Utils();
    private Context mContext;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = getContext();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragmentView = inflater.inflate(R.layout.fragment_history, container, false);

        mRecyclerView = mFragmentView.findViewById(R.id.history_recycler);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mItemAdapter = new HistoryItemAdapter();

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT,ItemTouchHelper.RIGHT){
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mItemAdapter.onItemDismiss(viewHolder.getBindingAdapterPosition());
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        refreshList();
        return mFragmentView;
    }

    public void refreshList(){
        mRecyclerView.swapAdapter(mItemAdapter,true);
        LinearLayout emptyView = mFragmentView.findViewById(R.id.title_no_data);
        if (mItemAdapter.getItemCount()==0) {
            emptyView.setVisibility(View.VISIBLE);
        }else{
            emptyView.setVisibility(View.GONE);
        }
    }

    private class HistoryItemAdapter extends RecyclerView.Adapter<ItemViewHolder>{

        Cursor cursor;

        HistoryItemAdapter(){
            resetCursor();
        }

        void resetCursor(){
            DBHelper dbHelper = new DBHelper(mContext);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            cursor = db.query("history",null,null,null,null,null,"time DESC");
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
            final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.history_item,viewGroup,false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            cursor.moveToPosition(position);

            CursorHelper helper = new CursorHelper(cursor);

            //long itemID = cursor.getLong(cursor.getColumnIndex("_id"));
            int codeType = helper.getInt("codeType");
            final String codeValue = helper.getString("codeValue");

            holder.date.setText(helper.getString("date"));
            holder.type.setText(utils.nameOfBarcodeFormat(codeType));
            holder.value.setText(codeValue);

            switch (codeType){
                case Barcode.FORMAT_QR_CODE:
                    holder.icon.setImageResource(R.drawable.qr_code_48);
                    break;
                case Barcode.FORMAT_EAN_13:
                    holder.icon.setImageResource(R.drawable.barcode_48);
                    break;
                default:
                    holder.icon.setImageResource(R.drawable.product_48);
            }

            holder.itemView.setOnClickListener((View view) -> {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT,codeValue);
                    intent.setType("text/plain");
                    startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return cursor.getCount();
        }

        @Override
        public long getItemId(int position) {
            cursor.moveToPosition(position);
            CursorHelper helper = new CursorHelper(cursor);
            return helper.getLong("raw_id");
        }

        void onItemDismiss(int position){
            long itemID = getItemId(position);
            DBHelper dbHelper = new DBHelper(mContext);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete("history","_id="+itemID,null);
            resetCursor();
            mItemAdapter.notifyItemRemoved(position);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder{

        TextView date;
        TextView type;
        TextView value;
        ImageView icon;

        ItemViewHolder(View view){
            super(view);
            date = view.findViewById(R.id.item_date);
            type = view.findViewById(R.id.item_type);
            value = view.findViewById(R.id.item_value);
            icon = view.findViewById(R.id.item_icon);
        }
    }
}
