package uren.com.myduties.utils.layoutManager;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

public class CustomGridLayoutManager extends GridLayoutManager {


    private static final float MILLISECONDS_PER_INCH = 10f;
    private Context mContext;

    public CustomGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        mContext = context;
    }

    public CustomGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView,
                                       RecyclerView.State state, final int position) {

        LinearSmoothScroller smoothScroller =
                new LinearSmoothScroller(mContext) {

                    //This controls the direction in which smoothScroll looks
                    //for your view
                    @Override
                    public PointF computeScrollVectorForPosition
                    (int targetPosition) {
                        return CustomGridLayoutManager.this
                                .computeScrollVectorForPosition(targetPosition);
                    }

                    //This returns the milliseconds it takes to
                    //scroll one pixel.
                    @TargetApi(Build.VERSION_CODES.DONUT)
                    @Override
                    protected float calculateSpeedPerPixel
                    (DisplayMetrics displayMetrics) {
                        return MILLISECONDS_PER_INCH/displayMetrics.densityDpi;
                    }
                };

        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }


}
