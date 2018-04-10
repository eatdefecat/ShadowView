package com.android.dz.shadow;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.TRANSPARENT;
import static android.graphics.PorterDuff.Mode.SRC_IN;


public class ShadowView extends View {

    private Paint mPaintShadow;
    private int mPadding = dp2px(10);
    private int mRound;
    private Bitmap mShadowBitmap;
    private View mRootView;
    private static final String TAG = "ShadowLayout";

    public ShadowView(Context context, View view) {
        super(context);
        mRootView = view;
    }

    public ShadowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ShadowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initView(){
        try {
            mRootView.post(new Runnable() {
                @Override
                public void run() {
                    ViewGroup parent = (ViewGroup) mRootView.getParent();
                    if(parent != null){
                        RelativeLayout relativeLayout = new RelativeLayout(getContext());
                        parent.addView(relativeLayout, mRootView.getLayoutParams());
                        parent.removeView(mRootView);

                        ViewGroup.LayoutParams layoutParams = mRootView.getLayoutParams();
                        layoutParams.height = mRootView.getMeasuredHeight() + (mPadding * 2);
                        layoutParams.width = mRootView.getMeasuredWidth();

                        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(layoutParams);
                        lps.setMargins(mRootView.getPaddingLeft(), mRootView.getPaddingTop(), mRootView.getPaddingRight(), mRootView.getPaddingBottom());
                        relativeLayout.addView(ShadowView.this, lps);

                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(layoutParams);
                        lp.setMargins(mPadding, mPadding, mPadding, mPadding);
                        relativeLayout.addView(mRootView, lp);

                        mPaintShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
                        mPaintShadow.setColorFilter(new PorterDuffColorFilter(BLACK, SRC_IN));
                        mPaintShadow.setAlpha(15);

                        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    }
                }
            });
        }catch (Throwable e){
            Log.e(TAG, "errShadowLayout=====" + e.getMessage());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "==================onDraw");
        try {
            if(mShadowBitmap == null){
                drawShadow(canvas);
            }else{
                canvas.drawBitmap(mShadowBitmap, 0, 0, null);
            }
        }catch (Throwable e){
            Log.e(TAG, "errOnDraw=====" + e.getMessage());
        }
    }

    private void drawShadow(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mShadowBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ALPHA_8);
            mShadowBitmap.eraseColor(TRANSPARENT);
            Canvas c = new Canvas(mShadowBitmap);
            Path path = new Path();
            RectF f = new RectF(mPadding, mPadding, getWidth() - mPadding, getHeight() - mPadding);
            path.addRoundRect(f, dp2px(mRound), dp2px(mRound), Path.Direction.CW);
            c.drawPath(path, mPaintShadow);

            RenderScript rs = RenderScript.create(getContext());
            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8(rs));
            Allocation input = Allocation.createFromBitmap(rs, mShadowBitmap);
            Allocation output = Allocation.createTyped(rs, input.getType());
            blur.setRadius(25);
            blur.setInput(input);
            blur.forEach(output);
            output.copyTo(mShadowBitmap);
            input.destroy();
            output.destroy();

            canvas.drawBitmap(mShadowBitmap, 0, 0, null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            if(mShadowBitmap != null){
                mShadowBitmap.recycle();
                mShadowBitmap = null;
            }
        }catch (Throwable e){
            Log.e(TAG, e.getMessage());
        }
    }

    public int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
                getContext().getResources().getDisplayMetrics());
    }

    public static ShadowView with(Context context, View view) {
        return new ShadowView(context, view);
    }

    public ShadowView setRound(int round){
        mRound = round;
        return this;
    }

    public ShadowView setmPadding(int padding){
        if(padding >= 10){
            mPadding = dp2px(padding);
        }
        return this;
    }
}