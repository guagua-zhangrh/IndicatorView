package com.zhangrenhua.indicatorview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


/**
 * 圆形指示器
 */
public class CircleIndicatorView extends View {
    private static final String TAG = "CircleIndicatorView";
    private ViewPager mViewPager;
    private List<ShapeHolder> tabItems;
    private ShapeHolder movingItem;

    //config list
    private int mCurItemPosition;
    private float mCurItemPositionOffset;
    private float mIndicatorRadius;
    private float mIndicatorMargin;
    private int mIndicatorBackground;
    private int mIndicatorSelectedBackground;
    private Gravity mIndicatorLayoutGravity;
    private Mode mIndicatorMode;
    private float mMovingScaleRatio;

    //default value
    private final int DEFAULT_INDICATOR_RADIUS = 10;
    private final int DEFAULT_INDICATOR_MARGIN = 40;
    private final int DEFAULT_INDICATOR_BACKGROUND = Color.BLUE;
    private final int DEFAULT_INDICATOR_SELECTED_BACKGROUND = Color.RED;
    private final int DEFAULT_INDICATOR_LAYOUT_GRAVITY = Gravity.CENTER.ordinal();
    private final int DEFAULT_INDICATOR_MODE = Mode.SOLO.ordinal();
    //按比例缩放，1为不缩放
    private final float DEFAULT_MOVING_SCALE_RATIO = 1.0F;

    public enum Gravity {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum Mode {
        INSIDE,
        OUTSIDE,
        SOLO
    }

    public CircleIndicatorView(Context context) {
        super(context);
        init(context, null);
    }

    public CircleIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        tabItems = new ArrayList<>();
        handleTypedArray(context, attrs);
    }

    private void handleTypedArray(Context context, AttributeSet attrs) {
        if (attrs == null)
            return;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleIndicatorView);
        mIndicatorRadius = typedArray.getDimensionPixelSize(R.styleable.CircleIndicatorView_ci_radius, DEFAULT_INDICATOR_RADIUS);
        mIndicatorMargin = typedArray.getDimensionPixelSize(R.styleable.CircleIndicatorView_ci_margin, DEFAULT_INDICATOR_MARGIN);
        mIndicatorBackground = typedArray.getColor(R.styleable.CircleIndicatorView_ci_background, DEFAULT_INDICATOR_BACKGROUND);
        mIndicatorSelectedBackground = typedArray.getColor(R.styleable.CircleIndicatorView_ci_selected_background, DEFAULT_INDICATOR_SELECTED_BACKGROUND);
        int gravity = typedArray.getInt(R.styleable.CircleIndicatorView_ci_gravity, DEFAULT_INDICATOR_LAYOUT_GRAVITY);
        mIndicatorLayoutGravity = Gravity.values()[gravity];
        int mode = typedArray.getInt(R.styleable.CircleIndicatorView_ci_mode, DEFAULT_INDICATOR_MODE);
        mIndicatorMode = Mode.values()[mode];
        mMovingScaleRatio = typedArray.getFloat(R.styleable.CircleIndicatorView_ci_moving_scale_ratio, DEFAULT_MOVING_SCALE_RATIO);
        typedArray.recycle();
    }

    public void setViewPager(ViewPager viewPager) {
        this.mViewPager = viewPager;
        this.mCurItemPosition = mViewPager.getCurrentItem();
        createTabItems();
        createMovingItem();
        setUpListener();
        requestLayout();
        invalidate();
    }

    public void invalidateView() {
        this.mCurItemPosition = mViewPager.getCurrentItem();
        createTabItems();
        createMovingItem();
        requestLayout();
        invalidate();
    }

    private void setUpListener() {
        mViewPager.removeOnPageChangeListener(mSimpleOnPageChangeListener);
        mViewPager.addOnPageChangeListener(mSimpleOnPageChangeListener);
    }

    private ViewPager.SimpleOnPageChangeListener mSimpleOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            if (mIndicatorMode != Mode.SOLO) {
                trigger(position, positionOffset);
            }
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (mIndicatorMode == Mode.SOLO) {
                trigger(position, 0);
            }
        }
    };

    /**
     * trigger to redraw the indicator when the ViewPager's selected item changed!
     *
     * @param position
     * @param positionOffset
     */
    private void trigger(int position, float positionOffset) {
        CircleIndicatorView.this.mCurItemPosition = position;
        CircleIndicatorView.this.mCurItemPositionOffset = positionOffset;
        Log.e(TAG, "onPageScrolled()" + position + ":" + positionOffset);
        requestLayout();
        invalidate();
    }

    private void createTabItems() {
        tabItems.clear();
        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            OvalShape circle = new OvalShape();
            ShapeDrawable drawable = new ShapeDrawable(circle);
            ShapeHolder shapeHolder = new ShapeHolder(drawable);
            Paint paint = drawable.getPaint();
            paint.setColor(mIndicatorBackground);
            paint.setAntiAlias(true);
            shapeHolder.setPaint(paint);
            tabItems.add(shapeHolder);
        }
    }

    private void createMovingItem() {
        OvalShape circle = new OvalShape();
        ShapeDrawable drawable = new ShapeDrawable(circle);
        movingItem = new ShapeHolder(drawable);
        Paint paint = drawable.getPaint();
        paint.setColor(mIndicatorSelectedBackground);
        paint.setAntiAlias(true);

        switch (mIndicatorMode) {
            case INSIDE:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                break;
            case OUTSIDE:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                break;
            case SOLO:
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                break;
        }

        movingItem.setPaint(paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        int intrinsicWidthSize = (int) ((mIndicatorRadius * 2 + mIndicatorMargin) *
                mViewPager.getAdapter().getCount() - mIndicatorMargin +
                (mIndicatorRadius * 2 * mMovingScaleRatio) - mIndicatorRadius * 2);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(intrinsicWidthSize, widthSize);
        } else {
            width = intrinsicWidthSize;
        }
        int intrinsicheightSize = (int) (mIndicatorRadius * 2 * mMovingScaleRatio);
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(intrinsicheightSize, heightSize);
        } else {
            height = intrinsicheightSize;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.e("CircleIndicator", "onLayout()");
        super.onLayout(changed, left, top, right, bottom);
        final int width = getWidth();
        final int height = getHeight();
        layoutTabItems(width, height);
        layoutMovingItem(mCurItemPosition, mCurItemPositionOffset);
    }

    private void layoutTabItems(final int containerWidth, final int containerHeight) {
        if (tabItems == null) {
            throw new IllegalStateException("forget to create tabItems?");
        }
        final float yCoordinate = containerHeight * 0.5f;
        final float startPosition = startDrawPosition(containerWidth);
        for (int i = 0; i < tabItems.size(); i++) {
            ShapeHolder item = tabItems.get(i);
            item.resizeShape(2 * mIndicatorRadius, 2 * mIndicatorRadius);
            item.setY(yCoordinate - mIndicatorRadius);
            float x = startPosition + (mIndicatorMargin + mIndicatorRadius * 2) * i;
            item.setX(x);
        }
    }

    private float startDrawPosition(final int containerWidth) {
        if (mIndicatorLayoutGravity == Gravity.LEFT)
            return 0;
        float tabItemsLength = tabItems.size() * (2 * mIndicatorRadius + mIndicatorMargin) - mIndicatorMargin;
        if (containerWidth < tabItemsLength) {
            return 0;
        }
        if (mIndicatorLayoutGravity == Gravity.CENTER) {
            return (containerWidth - tabItemsLength) / 2;
        }
        return containerWidth - tabItemsLength;
    }

    private void layoutMovingItem(final int position, final float positionOffset) {
        if (movingItem == null) {
            throw new IllegalStateException("forget to create movingItem?");
        }

        if (tabItems.size() == 0) {
            return;
        }
        ShapeHolder item = tabItems.get(position);
        movingItem.resizeShape(item.getWidth() * mMovingScaleRatio, item.getHeight() * mMovingScaleRatio);
        float x = item.getX() + (mIndicatorMargin + mIndicatorRadius * 2) * positionOffset - (item.getWidth() * mMovingScaleRatio - item.getWidth()) / 2;
        movingItem.setX(x);
        movingItem.setY(item.getY() - (item.getHeight() * mMovingScaleRatio - item.getHeight()) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e("CircleIndicator", "onDraw()");
        super.onDraw(canvas);
        int sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), null,
                Canvas.MATRIX_SAVE_FLAG |
                        Canvas.CLIP_SAVE_FLAG |
                        Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                        Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                        Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        for (ShapeHolder item : tabItems) {
            drawItem(canvas, item);
        }

        if (movingItem != null) {
            drawItem(canvas, movingItem);
        }
        canvas.restoreToCount(sc);
    }

    private void drawItem(Canvas canvas, ShapeHolder shapeHolder) {
        canvas.save();
        canvas.translate(shapeHolder.getX(), shapeHolder.getY());
        shapeHolder.getShape().draw(canvas);
        canvas.restore();
    }

    public void setIndicatorRadius(float mIndicatorRadius) {
        this.mIndicatorRadius = mIndicatorRadius;
    }

    public void setIndicatorMargin(float mIndicatorMargin) {
        this.mIndicatorMargin = mIndicatorMargin;
    }

    public void setIndicatorBackground(int mIndicatorBackground) {
        this.mIndicatorBackground = mIndicatorBackground;
    }

    public void setIndicatorSelectedBackground(int mIndicatorSelectedBackground) {
        this.mIndicatorSelectedBackground = mIndicatorSelectedBackground;
    }

    public void setIndicatorLayoutGravity(Gravity mIndicatorLayoutGravity) {
        this.mIndicatorLayoutGravity = mIndicatorLayoutGravity;
    }

    public void setIndicatorMode(Mode mIndicatorMode) {
        this.mIndicatorMode = mIndicatorMode;
    }
}
