package com.georgeren.androidcustomview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by georgeRen on 2018/3/6.
 */

public class MyFlipPager extends FrameLayout {
    private static final String TAG = "PictureScrollView4";
    private Paint mClearPaint;
    private Bitmap reel;
    private float mReelWidth, mReelHeight;
    private RectF dstReel = new RectF();
    private int mWidth;
    private int mHeight;
    private float mProgress;
    private RectF rect = new RectF();
    private boolean startDrawProgress = false;
    private long completelyOpenTime = 3000;
    private float triangleX = 0;
    private float moveX = 0;
    private boolean isRight = false;

    public MyFlipPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.MyFlipPager);
        int rellId = mTypedArray.getResourceId(R.styleable.MyFlipPager_control_pic, 0);
        reel = BitmapFactory.decodeResource(getResources(), rellId);
        mTypedArray.recycle();
        init();
    }

    public MyFlipPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyFlipPager(Context context) {
        this(context, null);
    }

    private void init() {
        mClearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClearPaint.setColor(Color.BLUE);
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        if (mReelWidth == 0) {
            mReelHeight = mHeight;
            mReelWidth = reel.getWidth();
        }
        dstReel.set(mReelWidth, mHeight / 2 - mReelHeight / 2, 0, mHeight / 2 + mReelHeight / 2);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        rect.set(canvas.getClipBounds());
        int saveCount = canvas.saveLayer(rect, null, Canvas.ALL_SAVE_FLAG);//这里null在xml预览报错,要不报错传个全局new paint吧.
        super.dispatchDraw(canvas);
        Path path = new Path();
        if (isRight) {
            path.moveTo(0, 0);
            path.lineTo(-dip2px(50) + mProgress, 0);
            path.lineTo(-dip2px(100) + mProgress, mHeight);
            path.lineTo(0, mHeight);
        } else {
            path.moveTo(mWidth + mReelWidth, 0);
            path.lineTo(-dip2px(50) + mProgress, 0);
            path.lineTo(-dip2px(100) + mProgress, mHeight);
            path.lineTo(mWidth + mReelWidth, mHeight);
        }

        path.close();
        canvas.drawPath(path, mClearPaint);//控制显示区域
        canvas.restoreToCount(saveCount);
        canvas.drawBitmap(reel, null, dstReel, null);
    }

    private int direction;
    private final int none = 0;
    private final int left = 1;
    private final int right = 2;
    public int currentPosition;
    public int minPosition;
    public int maxPosition;
    public boolean isCanFlip = true;
    private ValueAnimator animator;
    private ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animator) {
            float v = (Float) animator.getAnimatedValue();
            mProgress = v;
            dstReel.left = -mReelWidth + mProgress;//0-->
            dstReel.right = mProgress;
            invalidate();
        }
    };


    public boolean isAniming;

    public void click2Turn(final boolean is2Right, final boolean isClick, final int curPosition, final int clickPosition) {
        if (isAniming) {
            return;
        }
        isAniming = true;
        if (animator != null) {
            animator.cancel();
        }
        if (is2Right) {
            if (mProgress >= mWidth + mReelWidth) {
                mProgress = 0;
            }
            animator = ValueAnimator.ofFloat(mProgress, mWidth + mReelWidth);
        } else {
            if (mProgress <= 0) {
                mProgress = mWidth + mReelWidth;
            }
            animator = ValueAnimator.ofFloat(mProgress, 0);
        }

        animator.addUpdateListener(updateListener);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAniming = false;
                if (isClick) {
                    pagerFlipListener.animClickComplete(curPosition, clickPosition);
                } else {
                    if (is2Right) {
                        if (pagerFlipListener != null) {
                            pagerFlipListener.animCompletePre();
                        }
                    } else {
                        if (pagerFlipListener != null) {
                            pagerFlipListener.animCompleteNext();
                        }
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAniming = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                isAniming = true;
            }
        });
        animator.setDuration((long) (completelyOpenTime));// 1920-?
        animator.setInterpolator(new DecelerateInterpolator());
        isRight = is2Right;
        animator.start();
    }


    public void left2Right() {
        if (isAniming) {
            return;
        }
        isAniming = true;
        if (animator != null) {
            animator.cancel();
        }
        if (mProgress >= mWidth + mReelWidth) {
            mProgress = 0;
        }
        animator = ValueAnimator.ofFloat(mProgress, mWidth + mReelWidth);
        animator.addUpdateListener(updateListener);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAniming = false;
                if (pagerFlipListener != null) {
                    pagerFlipListener.animCompletePre();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAniming = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                isAniming = true;
            }
        });
        animator.setDuration((long) ((mWidth + mReelWidth - (mProgress)) / (mWidth + mReelWidth) * completelyOpenTime));// 1920-?
        animator.setInterpolator(new DecelerateInterpolator());
        isRight = true;
        animator.start();
    }

    public void right2Left() {
        if (isAniming) {
            return;
        }
        isAniming = true;
        if (animator != null) {
            animator.cancel();
        }
        if (mProgress <= 0) {
            mProgress = mWidth + mReelWidth;
        }
        animator = ValueAnimator.ofFloat(mProgress, 0);
        animator.addUpdateListener(updateListener);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAniming = false;
                if (pagerFlipListener != null) {
                    pagerFlipListener.animCompleteNext();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAniming = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                isAniming = true;
            }
        });

        animator.setDuration((long) (mProgress / (mWidth + mReelWidth) * completelyOpenTime));
        animator.setInterpolator(new DecelerateInterpolator());
        isRight = false;
        animator.start();
    }

    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private PagerFlipListener pagerFlipListener;

    public void setPagerFlipListener(PagerFlipListener pagerFlipListener) {
        this.pagerFlipListener = pagerFlipListener;
    }

    public interface PagerFlipListener {
        void animCompletePre();

        void animCompleteNext();

        void animClickComplete(int curPosition, int clickPosition);
    }
}
