package cn.edu.stu.max.cocovendor.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.edu.stu.max.cocovendor.R;
import cn.edu.stu.max.cocovendor.databaseClass.Goods;

public class CabinetMonthlySalesAnalyzeAdapter extends RecyclerView.Adapter {
    private List<Goods> list;
    private Context context;

    public CabinetMonthlySalesAnalyzeAdapter(List<Goods> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cabinet_monthly_sales_analyze_item, parent, false);
        return new sheetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        sheetViewHolder vh = (sheetViewHolder) holder;

        if (list.get(position).getName() == null) {
        } else {
            vh.getTv_sheetRow3().setText(String.valueOf(list.get(position)));
            vh.getTv_sheetRow4().setText(String.valueOf(list.get(position).getImage_path()));
            vh.getBtn_sheetRow5().setText(String.valueOf(list.get(position).getSales_price()));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class sheetViewHolder extends RecyclerView.ViewHolder{
        private final View mView;
        private final TextView tv_sheetRow3;
        private final TextView tv_sheetRow4;
        private final TextView btn_sheetRow5;

        private sheetViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            tv_sheetRow3 = (TextView) itemView.findViewById(R.id.text_selling_price);
            tv_sheetRow4 = (TextView) itemView.findViewById(R.id.text_cash_times);
            btn_sheetRow5 = (TextView) itemView.findViewById(R.id.text_wechat_times);
        }



        private TextView getTv_sheetRow3() {
            return tv_sheetRow3;
        }

        private TextView getTv_sheetRow4() {
            return tv_sheetRow4;
        }

        public TextView getBtn_sheetRow5() {
            return btn_sheetRow5;
        }

    }
}
