package ch.epfl.sweng.project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Class that represent the divider used in the recycler view.
 * It draws a line between each item of the list.
 */
class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private final Drawable mDivider;

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public DividerItemDecoration(Context context) {
        mDivider = context.getResources().getDrawable(R.drawable.line_divider, null);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}