package com.sovell.retail_cabinet.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.sovell.retail_cabinet.R;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class RangeSlider extends View {

    private static final int HORIZONTAL_PADDING = 100;
    private static final int DEFAULT_TOUCH_TARGET_SIZE = Math.round(dpToPx(40));

    private Paint linePaint;
    private Paint textPaint;

    private float unpressedRadius;
    private float pressedRadius;
    private float minTargetRadius = 0;
    private float maxTargetRadius = 0;
    private float convertFactor;
    private float insideRangeLineStrokeWidth;
    private float outsideRangeLineStrokeWidth;

    private boolean lastTouchedMin;
    private int lineStartX;
    private int lineEndX;
    private int minPosition = 0;
    private int maxPosition = 0;
    private int midY = 0;
    private int min = 4;
    private int max = 25;
    private int range;

    private int targetColor;
    private int insideRangeColor;
    private int outsideRangeColor;

    private Set<Integer> isTouchingMinTarget = new HashSet<>();
    private Set<Integer> isTouchingMaxTarget = new HashSet<>();

    private RangeSliderListener rangesliderListener;

    private ObjectAnimator minAnimator;
    private ObjectAnimator maxAnimator;

    private Integer startingMin;
    private Integer startingMax;

    public RangeSlider(Context context) {
        super(context);
        init(null);
    }

    public RangeSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RangeSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray styledAttrs = getContext().obtainStyledAttributes(attrs, R.styleable.RangeSlider, 0, 0);
            targetColor = styledAttrs.getColor(R.styleable.RangeSlider_targetColor, Color.BLUE);
            insideRangeColor = styledAttrs.getColor(R.styleable.RangeSlider_insideRangeLineColor, Color.GREEN);
            outsideRangeColor = styledAttrs.getColor(R.styleable.RangeSlider_outsideRangeLineColor, Color.YELLOW);
            min = styledAttrs.getInt(R.styleable.RangeSlider_min, 4);
            max = styledAttrs.getInt(R.styleable.RangeSlider_max, 25);

            unpressedRadius = styledAttrs.getDimension(R.styleable.RangeSlider_unpressedTargetRadius, 15);
            pressedRadius = styledAttrs.getDimension(R.styleable.RangeSlider_pressedTargetRadius, 40);
            insideRangeLineStrokeWidth = styledAttrs.getDimension(R.styleable.RangeSlider_insideRangeLineStrokeWidth, 8);
            outsideRangeLineStrokeWidth = styledAttrs.getDimension(R.styleable.RangeSlider_outsideRangeLineStrokeWidth, 4);

            styledAttrs.recycle();
        }

        minTargetRadius = unpressedRadius;
        maxTargetRadius = unpressedRadius;
        range = max - min;

        minAnimator = getMinTargetAnimator(true);
        maxAnimator = getMaxTargetAnimator(true);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int desiredHeight = (int) (pressedRadius * 10);

        int width = widthSize;
        int height = desiredHeight;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(widthSize, widthSize);
        } else {
            width = widthSize;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = desiredHeight;
        }

        int lineLength = (width - HORIZONTAL_PADDING * 2);
        midY = height / 2;
        lineStartX = HORIZONTAL_PADDING;
        lineEndX = lineLength + HORIZONTAL_PADDING;

        convertFactor = ((float) range) / lineLength;

        setSelectedMin(startingMin != null ? startingMin : min);
        setSelectedMax(startingMax != null ? startingMax : max);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawEntireRangeLine(canvas);
        drawSelectedRangeLine(canvas);
        drawSelectedTargets(canvas);

        textPaint.setTextSize(26);
        textPaint.setColor(Color.parseColor("#484E63"));
        canvas.drawText(String.format(Locale.CHINA, "%d℃", getSelectedMin()), minPosition - pressedRadius, midY - pressedRadius - 10, textPaint);
        canvas.drawText(String.format(Locale.CHINA, "%d℃", getSelectedMax()), maxPosition - pressedRadius, midY - pressedRadius - 10, textPaint);

        textPaint.setTextSize(24);
        textPaint.setColor(Color.parseColor("#A2A5B0"));
        canvas.drawText(String.format(Locale.CHINA, "%d℃", min), lineStartX - pressedRadius, midY + 3 * pressedRadius, textPaint);
        canvas.drawText(String.format(Locale.CHINA, "%d℃", max), lineEndX - pressedRadius, midY + 3 * pressedRadius, textPaint);
    }

    private void drawEntireRangeLine(Canvas canvas) {
        linePaint.setColor(outsideRangeColor);
        linePaint.setStrokeWidth(outsideRangeLineStrokeWidth);
        canvas.drawLine(lineStartX, midY, lineEndX, midY, linePaint);
    }

    private void drawSelectedRangeLine(Canvas canvas) {
        linePaint.setStrokeWidth(insideRangeLineStrokeWidth);
        linePaint.setColor(insideRangeColor);
        canvas.drawLine(minPosition, midY, maxPosition, midY, linePaint);
    }

    private void drawSelectedTargets(Canvas canvas) {
        linePaint.setColor(targetColor);
        canvas.drawCircle(minPosition, midY, minTargetRadius, linePaint);
        canvas.drawCircle(maxPosition, midY, maxTargetRadius, linePaint);
    }

    private void jumpToPosition(int index, MotionEvent event) {
        if (event.getX(index) > maxPosition && event.getX(index) <= lineEndX) {
            maxPosition = (int) event.getX(index);
            invalidate();
            callMaxChangedCallbacks();
        } else if (event.getX(index) < minPosition && event.getX(index) >= lineStartX) {
            minPosition = (int) event.getX(index);
            invalidate();
            callMinChangedCallbacks();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        final int actionIndex = event.getActionIndex();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (lastTouchedMin) {
                    if (!checkTouchingMinTarget(actionIndex, event) && !checkTouchingMaxTarget(actionIndex, event)) {
                        jumpToPosition(actionIndex, event);
                    }
                } else if (!checkTouchingMaxTarget(actionIndex, event) && !checkTouchingMinTarget(actionIndex, event)) {
                    jumpToPosition(actionIndex, event);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                isTouchingMinTarget.remove(event.getPointerId(actionIndex));
                isTouchingMaxTarget.remove(event.getPointerId(actionIndex));
                if (isTouchingMinTarget.isEmpty()) {
                    minAnimator.cancel();
                    minAnimator = getMinTargetAnimator(false);
                    minAnimator.start();
                }
                if (isTouchingMaxTarget.isEmpty()) {
                    maxAnimator.cancel();
                    maxAnimator = getMaxTargetAnimator(false);
                    maxAnimator.start();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    if (isTouchingMinTarget.contains(event.getPointerId(i))) {
                        int touchX = (int) event.getX(i);
                        touchX = clamp(touchX, lineStartX, lineEndX);
                        if (touchX >= maxPosition) {
                            maxPosition = touchX;
                            callMaxChangedCallbacks();
                        }
                        minPosition = touchX;
                        callMinChangedCallbacks();
                    }
                    if (isTouchingMaxTarget.contains(event.getPointerId(i))) {
                        int touchX = (int) event.getX(i);
                        touchX = clamp(touchX, lineStartX, lineEndX);
                        if (touchX <= minPosition) {
                            minPosition = touchX;
                            callMinChangedCallbacks();
                        }
                        maxPosition = touchX;
                        callMaxChangedCallbacks();
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    if (lastTouchedMin) {
                        if (!checkTouchingMinTarget(i, event) && !checkTouchingMaxTarget(i, event)) {
                            jumpToPosition(i, event);
                        }
                    } else if (!checkTouchingMaxTarget(i, event) && !checkTouchingMinTarget(i, event)) {
                        jumpToPosition(i, event);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                isTouchingMinTarget.clear();
                isTouchingMaxTarget.clear();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Checks if given index is touching the min target.  If touching start animation.
     */
    private boolean checkTouchingMinTarget(int index, MotionEvent event) {
        if (isTouchingMinTarget(index, event)) {
            lastTouchedMin = true;
            isTouchingMinTarget.add(event.getPointerId(index));
            if (!minAnimator.isRunning()) {
                minAnimator = getMinTargetAnimator(true);
                minAnimator.start();
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if given index is touching the max target.  If touching starts animation.
     */
    private boolean checkTouchingMaxTarget(int index, MotionEvent event) {
        if (isTouchingMaxTarget(index, event)) {
            lastTouchedMin = false;
            isTouchingMaxTarget.add(event.getPointerId(index));
            if (!maxAnimator.isRunning()) {
                maxAnimator = getMaxTargetAnimator(true);
                maxAnimator.start();
            }
            return true;
        }
        return false;
    }

    private void callMinChangedCallbacks() {
        if (rangesliderListener != null) {
            rangesliderListener.onMinChanged(getSelectedMin());
        }
    }

    private void callMaxChangedCallbacks() {
        if (rangesliderListener != null) {
            rangesliderListener.onMaxChanged(getSelectedMax());
        }
    }

    private boolean isTouchingMinTarget(int pointerIndex, MotionEvent event) {
        return event.getX(pointerIndex) > minPosition - DEFAULT_TOUCH_TARGET_SIZE
                && event.getX(pointerIndex) < minPosition + DEFAULT_TOUCH_TARGET_SIZE
                && event.getY(pointerIndex) > midY - DEFAULT_TOUCH_TARGET_SIZE
                && event.getY(pointerIndex) < midY + DEFAULT_TOUCH_TARGET_SIZE;
    }

    private boolean isTouchingMaxTarget(int pointerIndex, MotionEvent event) {
        return event.getX(pointerIndex) > maxPosition - DEFAULT_TOUCH_TARGET_SIZE
                && event.getX(pointerIndex) < maxPosition + DEFAULT_TOUCH_TARGET_SIZE
                && event.getY(pointerIndex) > midY - DEFAULT_TOUCH_TARGET_SIZE
                && event.getY(pointerIndex) < midY + DEFAULT_TOUCH_TARGET_SIZE;
    }

    public int getSelectedMin() {
        return Math.round((minPosition - lineStartX) * convertFactor + min);
    }

    public int getSelectedMax() {
        return Math.round((maxPosition - lineStartX) * convertFactor + min);
    }

    public void setStartingMinMax(int startingMin, int startingMax) {
        this.startingMin = startingMin;
        this.startingMax = startingMax;
    }

    private void setSelectedMin(int selectedMin) {
        minPosition = Math.round(((selectedMin - min) / convertFactor) + lineStartX);
        callMinChangedCallbacks();
    }

    private void setSelectedMax(int selectedMax) {
        maxPosition = Math.round(((selectedMax - min) / convertFactor) + lineStartX);
        callMaxChangedCallbacks();
    }

    public void setRangeSliderListener(RangeSliderListener listener) {
        rangesliderListener = listener;
    }

    public RangeSliderListener getRangeSliderListener() {
        return rangesliderListener;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
        range = max - min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
        range = max - min;
    }

    /**
     * Resets selected values to MIN and MAX.
     */
    public void reset() {
        minPosition = lineStartX;
        maxPosition = lineEndX;
        if (rangesliderListener != null) {
            rangesliderListener.onMinChanged(getSelectedMin());
            rangesliderListener.onMaxChanged(getSelectedMax());
        }
        invalidate();
    }

    public void setMinTargetRadius(float minTargetRadius) {
        this.minTargetRadius = minTargetRadius;
    }

    public void setMaxTargetRadius(float maxTargetRadius) {
        this.maxTargetRadius = maxTargetRadius;
    }

    /**
     * Keeps Number value inside min/max bounds by returning min or max if outside of
     * bounds.  Otherwise will return the value without altering.
     */
    private <T extends Number> T clamp(@NonNull T value, @NonNull T min, @NonNull T max) {
        if (value.doubleValue() > max.doubleValue()) {
            return max;
        } else if (value.doubleValue() < min.doubleValue()) {
            return min;
        }
        return value;
    }

    public static float dpToPx(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    private ObjectAnimator getMinTargetAnimator(boolean touching) {
        final ObjectAnimator anim = ObjectAnimator.ofFloat(this, "minTargetRadius", minTargetRadius, touching ? pressedRadius : unpressedRadius);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                anim.removeAllListeners();
                super.onAnimationEnd(animation);
            }
        });
        anim.setInterpolator(new AccelerateInterpolator());
        return anim;
    }

    private ObjectAnimator getMaxTargetAnimator(boolean touching) {
        final ObjectAnimator anim = ObjectAnimator.ofFloat(this, "maxTargetRadius", maxTargetRadius, touching ? pressedRadius : unpressedRadius);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                anim.removeAllListeners();
            }
        });
        anim.setInterpolator(new AccelerateInterpolator());
        return anim;
    }

    public interface RangeSliderListener {
        void onMaxChanged(int newValue);

        void onMinChanged(int newValue);
    }
}
