package com.example.teamnovapersonalprojectprojecting.ui.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.teamnovapersonalprojectprojecting.R;

import java.util.ArrayList;
import java.util.List;

public class TimeRangeViewGroup extends ViewGroup {
    private static final float MAX_VALUE = 100f;
    private static final  float BAR_WIDTH = 10f;
    private static final int HOUR_OF_DAY = 24;
    private static final int TIME_COLUM_WIDTH = 180;
    private List<RangeItem> rangeItems = new ArrayList<>();
    private int barColor = Color.GRAY;

    private static final int ONE_HOUR_SIZE = 200;

    public TimeRangeViewGroup(Context context) {
        super(context);
        init();
    }

    public TimeRangeViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeRangeViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Paint linePaint;
    private Paint linePaint2;
    private Paint textPaint;
    private void init(){
        setWillNotDraw(false);

        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(3);

        linePaint2 = new Paint();
        linePaint2.setColor(Color.BLACK);
        linePaint2.setStrokeWidth(7);


        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.RIGHT);

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        for(int i = 0; i <= HOUR_OF_DAY; i++){
            String hourText = String.format("%2d 시 ", i);
            canvas.drawText(hourText, TIME_COLUM_WIDTH, ONE_HOUR_SIZE * i, textPaint);

            canvas.drawLine(TIME_COLUM_WIDTH, ONE_HOUR_SIZE * i,  getWidth(), ONE_HOUR_SIZE * i, linePaint);
        }
        canvas.drawLine(TIME_COLUM_WIDTH, 0, TIME_COLUM_WIDTH, getHeight(), linePaint2);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            RangeItem item = rangeItems.get(i);
            int childTop = (int) (ONE_HOUR_SIZE * item.start);
            int childBottom = (int) (ONE_HOUR_SIZE * item.end);
            child.layout(TIME_COLUM_WIDTH + 3, childTop, r - l, childBottom);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int childWidthSpec = MeasureSpec.makeMeasureSpec(width - TIME_COLUM_WIDTH, MeasureSpec.EXACTLY);
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            RangeItem item = rangeItems.get(i);
            int childHeight = (int) ((item.end - item.start) * ONE_HOUR_SIZE);
            int childHeightSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
            child.measure(childWidthSpec - 3, childHeightSpec);
        }


        setMeasuredDimension(width,  ONE_HOUR_SIZE * HOUR_OF_DAY + 5); // 총 높이를 설정
    }

    public void clear(){
        rangeItems.clear();
        removeAllViews();
    }

    public void addRangeItemView(final RangeItem item) {
        TextView itemView = new TextView(getContext());
        itemView.setText(item.title);
        //itemView.setBackgroundColor(barColor);
        GradientDrawable border = new GradientDrawable();
        border.setColor(barColor);
        border.setStroke(6, Color.RED);
        border.setCornerRadius(8);
        itemView.setBackground(border);
        itemView.setTextSize(24);
        itemView.setGravity(Gravity.TOP | Gravity.START);
        itemView.setPadding(20,20,0,0);
        addView(itemView);
        rangeItems.add(item);

        itemView.setOnClickListener((view)->{
            if(item.listener != null){
                item.listener.onRangeItemClick();
            }
        });
        itemView.setOnLongClickListener((view)->{
            if(item.listener != null){
                return item.listener.onRangeItemLongClick();
            }
            return false;
        });
    }

    public static class RangeItem {
        float start;
        float end;
        String title;
        OnRangeItemClickListener listener;

        public RangeItem(float start, float end, String title) {
            this.start = start;
            this.end = end;
            this.title = title;
        }

        public RangeItem setListener(OnRangeItemClickListener listener) {
            this.listener = listener;
            return this;
        }
    }

    public interface OnRangeItemClickListener {
        void onRangeItemClick();
        default boolean onRangeItemLongClick(){
            return false;
        }
    }
}
