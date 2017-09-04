package com.polar.browser.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import com.polar.browser.utils.ColorUtils;

/**
 * The detector handle the touch event, and display a circle mask on your
 * canvas.
 */
public class MaterialBackgroundDetector {
	public static final int DEFAULT_COLOR = Color.BLACK;
	private static final String TAG = "MaterialBgDetector";
	private static final boolean DBG = true;
	private static final int DEFAULT_DURATION = 1200;
	private static final int DEFAULT_FAST_DURATION = 300;
	private static final int DEFAULT_TRANSPARENT_DURATION = 300;
	private static final int DEFAULT_ALPHA = 33;
	View mView;
	Callback mCallback;
	boolean mIsAnimation;
	private int mColor;
	private Paint mCirclePaint;
	private int mFocusColor;
	private int mCircleColor;
	// The position of the initial circle center.
	private float mX;
	private float mY;
	// The position of curr circle center.
	private float mCenterX;
	private float mCenterY;
	private float mViewRadius;
	// The radius of curr circle.
	private float mRadius;
	// The size of view.
	private int mWidth;
	private int mHeight;
	private int mMinPadding;
	private ObjectAnimator mAnimator;
	private boolean mIsFocused;

	private boolean mIsPerformClick;
	private boolean mIsPerformLongClick;

	private Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			mIsAnimation = true;
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mIsAnimation = false;
			mView.invalidate();
		}

		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}
	};

	private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

	public MaterialBackgroundDetector(Context context, View view,
									  Callback callback, int color) {
		mView = view;
		mCallback = callback;
		setColor(color);
		ViewConfiguration configuration = ViewConfiguration.get(context);
		mMinPadding = configuration.getScaledEdgeSlop();
	}

	private void setColor(int color) {
		if (mColor != color) {
			if (DBG) {
				Log.d(TAG, "ColorChanged");
			}
			mColor = color;
			setAlpha(DEFAULT_ALPHA);
		}
	}

	private void resetPaint() {
		if (mCirclePaint == null) {
			mCirclePaint = new Paint();
		}
		mCirclePaint.setColor(mCircleColor);
	}

	private int computeCircleColor(int color, int alpha) {
		return ColorUtils.getColorAtAlpha(color, alpha);
	}

	private int computeFocusColor(int color, int alpha) {
		return ColorUtils.getColorAtAlpha(color, alpha);
	}

	/**
	 * Called when view size changed.
	 *
	 * @param width
	 * @param height
	 */
	public void onSizeChanged(int width, int height) {
		mWidth = width;
		mHeight = height;
		mViewRadius = (float) Math.sqrt(mWidth * mWidth / 4 + mHeight * mHeight
				/ 4);
	}

	public boolean onTouchEvent(MotionEvent event, boolean result) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mIsFocused = true;
				if (!mIsAnimation) {
					// When the fingers touch the view, we let the mask appear
					// slowly.
					mX = event.getX();
					mY = event.getY();
					mAnimator = ObjectAnimator.ofFloat(this, "radius", mMinPadding,
							mViewRadius);
					int mDuration = DEFAULT_DURATION;
					mAnimator.setDuration(mDuration);
					mAnimator.setInterpolator(mInterpolator);
					mAnimator.addListener(mAnimatorListener);
					mAnimator.start();
					if (DBG) {
						Log.i(TAG, "Down,from:" + 0 + ",to:" + mViewRadius);
					}
				}
				// Ensure the following motion event can be received.
				if (!result) {
					result = true;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mIsFocused = false;
				cancelAnimator();
				mX = mCenterX;
				mY = mCenterY;
				mRadius = Math.max(mRadius, mViewRadius * 0.1f);
				int duration = (int) (DEFAULT_FAST_DURATION
						* (mViewRadius - mRadius) / mViewRadius);
				if (duration > 0) {
					// When the fingers leave the view, if the mask doesn't cover
					// whole view, we let the mask appear fast.
					mAnimator = ObjectAnimator.ofFloat(this, "radius", mRadius,
							mViewRadius);
					mAnimator.setDuration(duration);
					mAnimator.setInterpolator(mInterpolator);
					mAnimator.addListener(mAnimatorListener);
					mAnimator.start();
					if (DBG) {
						Log.i(TAG, "UP,from:" + mRadius + ",to:" + mViewRadius);
					}
				}
				// we should let the mask layer disappear gradually.
				ObjectAnimator alphaAnimator = ObjectAnimator.ofInt(this, "alpha",
						DEFAULT_ALPHA, 0);
				alphaAnimator.setDuration(DEFAULT_TRANSPARENT_DURATION);
				alphaAnimator.setInterpolator(new AccelerateInterpolator());
				alphaAnimator.addListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {
						mIsAnimation = true;
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						// When the animation end, we should do something.
						mIsAnimation = false;
						// Reset the alpha value.
						setAlpha(DEFAULT_ALPHA);
						// Handle the click event.
						if (mIsPerformClick) {
							if (mCallback != null) {
								mCallback.performClickAfterAnimation();
							}
							mIsPerformClick = false;
						}
						if (mIsPerformLongClick) {
							if (mCallback != null) {
								mCallback.performLongClickAfterAnimation();
							}
							mIsPerformLongClick = false;
						}
					}

					@Override
					public void onAnimationCancel(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}
				});
				alphaAnimator.start();
				mView.invalidate();
				break;
		}
		return result;
	}

	public void cancelAnimator() {
		if (mAnimator != null) {
			mAnimator.cancel();
		}
	}

	public void draw(Canvas canvas) {
		if (mIsFocused || mIsAnimation) {
			// If client is focused or in animation, show focus layer.
			if (DBG) {
				Log.d(TAG, "DrawFocusColor");
			}
			canvas.drawColor(mFocusColor);
			canvas.drawCircle(mCenterX, mCenterY, mRadius, mCirclePaint);
		}
	}

	/**
	 * This method is called by ObjectAnimator to invalidate mView.
	 *
	 * @param radius
	 */
	@SuppressWarnings("UnusedDeclaration")
	public void setRadius(float radius) {
		float percent = 0;
		if (mAnimator != null) {
			percent = mAnimator.getAnimatedFraction();
		}
		mRadius = radius;
		mCenterX = mX + percent * (mWidth / 2 - mX);
		mCenterY = mY + percent * (mHeight / 2 - mY);
		float distance = (float) Math.sqrt((mCenterX - mX) * (mCenterX - mX)
				+ (mCenterY - mY) * (mCenterY - mY))
				+ mMinPadding;
		if (distance > radius) {
			mCenterX = mX + (mCenterX - mX) * radius / distance;
			mCenterY = mY + (mCenterY - mY) * radius / distance;
		}
		mView.invalidate();
	}

	/**
	 * This method is called by ObjectAnimator to let the mask layer be
	 * transparent.
	 *
	 * @param alpha
	 */
	public void setAlpha(int alpha) {
		mFocusColor = computeFocusColor(mColor, alpha);
		mCircleColor = computeCircleColor(mColor, alpha);
		resetPaint();
		mView.invalidate();
	}

	/**
	 * It only happens when the fingers up. See also
	 * {@link #handlePerformLongClick()}
	 *
	 * @return whether it is been handled.
	 */
	public boolean handlePerformClick() {
		boolean result = mIsPerformClick;
		mIsPerformClick = true;
		return result;
	}

	/**
	 * It only happens when the fingers up. See also
	 * {@link #handlePerformClick()} ()}
	 *
	 * @return whether it is been handled.
	 */
	public boolean handlePerformLongClick() {
		boolean result = mIsPerformLongClick;
		mIsPerformLongClick = true;
		return result;
	}

	/**
	 * The Callback interface will call handle the click event after animation.
	 */
	public interface Callback {
		/**
		 * Handle click event after animation.
		 */
		void performClickAfterAnimation();

		/**
		 * Handle long click event after animation.
		 */
		void performLongClickAfterAnimation();
	}
}