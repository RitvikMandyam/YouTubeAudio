package com.wehavepi.youtubeaudio.adapters;

import android.view.View;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by williamwebb on 11/15/15.
 *
 * RecycleView Adapter class for use with DataBinding.
 */
public class DataBindingAdapter<DataType, BinderType extends ViewDataBinding> extends RecyclerView.Adapter<DataBindingAdapter.DataBindingViewHolder<DataType, BinderType>> {

    private final List<DataType> data;
    private final int            layoutId;
    private final int            variableId;

    /**
     * Constructor.
     *
     * @param data data to populate the Adapter with
     * @param layoutId layout used by the adapter
     * @param variableId variable id used to set DataBinding. Ex: BR.data
     */
    public DataBindingAdapter(List<DataType> data, @LayoutRes int layoutId, @LayoutRes int variableId) {
        this.data = data;
        this.layoutId = layoutId;
        this.variableId = variableId;
    }

    @Override
    public DataBindingViewHolder<DataType, BinderType> onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = View.inflate(parent.getContext(), layoutId, null);
        return new DataBindingViewHolder<>(v, variableId);
    }
    @Override
    public void onBindViewHolder(DataBindingViewHolder<DataType, BinderType> holder, int position) {
        holder.setData(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class DataBindingViewHolder<DataType, BinderType extends ViewDataBinding> extends ViewHolder {

        private final int variableId;
        private final BinderType dataBinding;

        public DataBindingViewHolder(View itemView, int variableId) {
            super(itemView);
            this.variableId = variableId;

            dataBinding = DataBindingUtil.bind(itemView);
        }

        public void setData(DataType data) {
            dataBinding.setVariable(variableId, data);
            dataBinding.executePendingBindings();
        }
    }
}
