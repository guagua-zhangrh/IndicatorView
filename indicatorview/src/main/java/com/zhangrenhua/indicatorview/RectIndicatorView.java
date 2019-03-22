package com.zhangrenhua.indicatorview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


/**
 * 矩形指示器
 */
public class RectIndicatorView extends View {
    private ViewPager mViewPager;
    private List<ShapeHolder> tabItems;
    private ShapeHolder movingItem;

    //config list
    private int mCurItemPosition;
    private float mCurItemPositionOffset;
    private float mIndicatorWidth;
    private float mIndicatorHeight;
    private float mIndicatorMargin;
    private int mIndicatorBackground;
    private int mIndicatorSelectedBackground;
    private Gravity mIndicatorLayoutGravity;
    private Mode mIndicatorMode;

    //default value
    private final int DEFAULT_INDICATOR_WIDTH = 10;
    private final int DEFAULT_INDICATOR_HEIGHT = 10;
    private final int DEFAULT_INDICATOR_MARGIN = 40;
    private final int DEFAULT_INDICATOR_BACKGROUND = Color.BLUE;
    private final int DEFAULT_INDICATOR_SELECTED_BACKGROUND = Color.RED;
    private final int DEFAULT_INDICATOR_LAYOUT_GRAVITY = Gravity.CENTER.ordinal();
    private final int DEFAULT_INDICATOR_MODE = Mode.SOLO.ordinal();

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

    public RectIndicatorView(Context context) {
        super(context);
        init(context, null);
    }

    public RectIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RectIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RectIndicatorView);
        mIndicatorWidth = typedArray.getDimensionPixelSize(R.styleable.RectIndicatorView_ri_width, DEFAULT_INDICATOR_WIDTH);
        mIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.RectIndicatorView_ri_height, DEFAULT_INDICATOR_HEIGHT);
        mIndicatorMargin = typedArray.getDimensionPixelSize(R.styleable.RectIndicatorView_ri_margin, DEFAULT_INDICATOR_MARGIN);
        mIndicatorBackground = typedArray.getColor(R.styleable.RectIndicatorView_ri_background, DEFAULT_INDICATOR_BACKGROUND);
        mIndicatorSelectedBackground = typedArray.getColor(R.styleable.RectIndicatorView_ri_selected_background, DEFAULT_INDICATOR_SELECTED_BACKGROUND);
        int gravity = typedArray.getInt(R.styleable.RectIndicatorView_ri_gravity, DEFAULT_INDICATOR_LAYOUT_GRAVITY);
        mIndicatorLayoutGravity = Gravity.values()[gravity];
        int mode = typedArray.getInt(R.styleable.RectIndicatorView_ri_mode, DEFAULT_INDICATOR_MODE);
        mIndicatorMode = Mode.values()[mode];
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
        RectIndicatorView.this.mCurItemPosition = position;
        RectIndicatorView.this.mCurItemPositionOffset = positionOffset;
        Log.e("RectIndicator", "onPageScrolled()" + position + ":" + positionOffset);
        requestLayout();
        invalidate();
    }

    private void createTabItems() {
        tabItems.clear();
        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            RectShape rectShape = new RectShape();
            ShapeDrawable drawable = new ShapeDrawable(rectShape);
            ShapeHolder shapeHolder = new ShapeHolder(drawable);
            Paint paint = drawable.getPaint();
            paint.setColor(mIndicatorBackground);
            paint.setAntiAlias(true);
            shapeHolder.setPaint(paint);
            tabItems.add(shapeHolder);
        }
    }

    private void createMovingItem() {
        RectShape rectShape = new RectShape();
        ShapeDrawable drawable = new ShapeDrawable(rectShape);
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
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.e("RectIndicator", "onLayout()");
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
            item.resizeShape(mIndicatorWidth, mIndicatorHeight);
            item.setY(yCoordinate - (mIndicatorHeight / 2));
            float x = startPosition + (mIndicatorMargin + mIndicatorWidth) * i;
            item.setX(x);
        }

    }

    private float startDrawPosition(final int containerWidth) {
        if (mIndicatorLayoutGravity == Gravity.LEFT)
            return 0;
        float tabItemsLength = tabItems.size() * (mIndicatorWidth + mIndicatorMargin) - mIndicatorMargin;
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
        movingItem.resizeShape(item.getWidth(), item.getHeight());
        float x = item.getX() + (mIndicatorMargin + mIndicatorWidth) * positionOffset;
        movingItem.setX(x);
        movingItem.setY(item.getY());

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        int intrinsicWidthSize = (int) ((mIndicatorWidth + mIndicatorMargin) * mViewPager.getAdapter().getCount() - mIndicatorMargin);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(intrinsicWidthSize, widthSize);
        } else {
            width = intrinsicWidthSize;
        }
        int intrinsicheightSize = (int) (mIndicatorHeight);
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
    protected void onDraw(Canvas canvas) {
        Log.e("RectIndicator", "onDraw()");
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

    public void setIndicatorWidth(float mIndicatorWidth) {
        this.mIndicatorWidth = mIndicatorWidth;
    }

    public void setIndicatorHeight(float mIndicatorHeight) {
        this.mIndicatorHeight = mIndicatorHeight;
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
