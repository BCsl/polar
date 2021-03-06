package org.sprite2d.apps.pp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.edmodo.cropper.i.ICropEdit;
import com.edmodo.cropper.i.IDrawEnd;

import org.sprite2d.apps.pp.util.Utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PainterCanvas extends SurfaceView implements Callback {

	private PainterThread mThread;
	private Bitmap mBitmap;
	private BrushPreset mPreset;

	// 瀛樺偍鎵�湁鐨勫姩浣�
	public static List<Action> mActionList;
	
	public static int mCurrentPaintIndex;
	
	private boolean mIsSetup;
	
	private static final String TAG = "PainterCanvas";

	public static final int BLUR_TYPE_NONE = 0;
	public static final int BLUR_TYPE_NORMAL = 1;
	public static final int BLUR_TYPE_INNER = 2;
	public static final int BLUR_TYPE_OUTER = 3;
	public static final int BLUR_TYPE_SOLID = 4;

	public PainterCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		mPreset = new BrushPreset(BrushPreset.PEN, Color.BLACK);

		setFocusable(true);
	}

	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (!hasWindowFocus) {
			getThread().freeze();
		} else {
			if (!isSetup()) {
				getThread().activate();
			} else {
				getThread().setup();
			}
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
//		SimpleLog.d(TAG, "surfaceChanged");
		if (mBitmap == null) {
			mBitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);

			getThread().setBitmap(mBitmap, true);
			IPainter painter = (IPainter) getContext();
			Bitmap bitmap = painter.getLastPicture();

			if (bitmap != null && !bitmap.isRecycled()) {
				float bitmapWidth = bitmap.getWidth();
				float bitmapHeight = bitmap.getHeight();
				float scale = 1.0f;
				
				Matrix matrix = new Matrix();
				if (width != bitmapWidth || height != bitmapHeight) {
					if (width == bitmapHeight || height == bitmapWidth) {
						if (width > height) {
							matrix.postRotate(-90, width / 2, height / 2);
						} else if (bitmapWidth != bitmapHeight) {
							matrix.postRotate(90, width / 2, height / 2);
						} else {
							if (painter.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
								matrix.postRotate(-90, width / 2, height / 2);
							}
						}
					} else {
						if (painter.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
							if (bitmapWidth > bitmapHeight
									&& bitmapWidth > width) {
								scale = (float) width / bitmapWidth;
							} else if (bitmapHeight > bitmapWidth
									&& bitmapHeight > height) {
								scale = (float) height / bitmapHeight;
							}
						} else {
							if (bitmapHeight > bitmapWidth
									&& bitmapHeight > height) {
								scale = (float) height / bitmapHeight;
							} else if (bitmapWidth > bitmapHeight
									&& bitmapWidth > width) {
								scale = (float) width / bitmapWidth;
							}
						}
					}

					if (scale == 1.0f) {
						matrix.preTranslate((width - bitmapWidth) / 2,
								(height - bitmapHeight) / 2);
					} else {
						matrix.postScale(scale, scale, bitmapWidth / 2,
								bitmapHeight / 2);
						matrix.postTranslate((width - bitmapWidth) / 2,
								(height - bitmapHeight) / 2);
					}
				}
				getThread().restoreBitmap(bitmap, matrix);
			}
		} else {
			
			IPainter painter = (IPainter) getContext();
			
			Bitmap bitmap = painter.getLastPicture();
			if (bitmap != null && !bitmap.isRecycled()) {
				
				float bitmapWidth = bitmap.getWidth();
				float bitmapHeight = bitmap.getHeight();
				
				float scale = 1.0f;
				Matrix matrix = new Matrix();
				if (width != bitmapWidth || height != bitmapHeight) {
					if (width == bitmapHeight || height == bitmapWidth) {
						if (width > height) {
							matrix.postRotate(-90, width / 2, height / 2);
						} else if (bitmapWidth != bitmapHeight) {
							matrix.postRotate(90, width / 2, height / 2);
						} else {
							if (painter.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
								matrix.postRotate(-90, width / 2, height / 2);
							}
						}
					} else {
						if (painter.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
							if (bitmapWidth > bitmapHeight
									&& bitmapWidth > width) {
								scale = (float) width / bitmapWidth;
							} else if (bitmapHeight > bitmapWidth
									&& bitmapHeight > height) {
								scale = (float) height / bitmapHeight;
							}
						} else {
							if (bitmapHeight > bitmapWidth
									&& bitmapHeight > height) {
								scale = (float) height / bitmapHeight;
							} else if (bitmapWidth > bitmapHeight
									&& bitmapWidth > width) {
								scale = (float) width / bitmapWidth;
							}
						}
					}

					if (scale == 1.0f) {
						matrix.preTranslate((width - bitmapWidth) / 2,
								(height - bitmapHeight) / 2);
					} else {
						matrix.postScale(scale, scale, bitmapWidth / 2,
								bitmapHeight / 2);
						matrix.postTranslate((width - bitmapWidth) / 2,
								(height - bitmapHeight) / 2);
					}
				}
				
				Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),  
						bitmap.getHeight(), matrix, true);
				getThread().setBitmap(resizedBitmap, false);
				
				getThread().restoreBitmap(bitmap, matrix);
				
			}
			
		}

		getThread().setPreset(mPreset);
		if (!isSetup()) {
			getThread().activate();
		} else {
			getThread().setup();
		}
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		getThread().on();
		getThread().start();
		getThread().initHandler();
	}

	@SuppressLint("NewApi")
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
//		SimpleLog.d(TAG, "surfaceDestroyed");
		getThread().off();
		getThread().quit();
//		SimpleLog.d("PainterCanvas", "holder -- surfaceDestroyed  mThread.getId()= " + mThread.getId());
		mThread = null;
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		if (!getThread().isReady()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			getThread().drawBegin(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_MOVE:
			getThread().draw(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			((ICropEdit)getContext()).setSaved(false);
			getThread().drawEnd(event.getX(), event.getY(), new IDrawEnd() {
				@Override
				public void end() {
					if (mCurrentPaintIndex == 1 && getContext() != null) {
						if (getContext() instanceof ICropEdit) {
							post(new Runnable() {
								public void run() {
									((ICropEdit)getContext()).setRevertEnabled(true);
								}
							});
						}
					}
				}
			});
			break;
		}
		return true;
	}

	public PainterThread getThread() {
		if (mThread == null) {
			mThread = new PainterThread(getHolder());
		}
		return mThread;
	}

	/*
	 * TODO: Make save quality changeable (possibly with a slider on a save
	 * dialogue?)
	 */
	public void saveBitmap(final String pictureName) throws FileNotFoundException {
		synchronized (getHolder()) {
			FileOutputStream fos = new FileOutputStream(pictureName);
			getThread().getBitmap().compress(CompressFormat.PNG, 80, fos);
			// 下载完成，刷新媒体库
			post(new Runnable() {
				@Override
				public void run() {
					Utils.refreshMediaMounted(getContext(), pictureName);
				}
			});
		}
	}
	
	public void saveBitmap(final String pictureName, final ISaveComplete iComplete) throws FileNotFoundException {
		synchronized (getHolder()) {
			FileOutputStream fos = new FileOutputStream(pictureName);
			getThread().getBitmap().compress(CompressFormat.PNG, 80, fos);
			//TODO  下载完成，刷新媒体库
			
			post(new Runnable() {
				@Override
				public void run() {
					Utils.refreshMediaMounted(getContext(), pictureName);
				}
			});
			
			if (iComplete != null) {
				post(new Runnable() {
					@Override
					public void run() {
						iComplete.complete();
					}
				});
				
			}
		}
	}

	
	
	/*
	 * NOTE: This is commented simply because it is unused as of now. If anyone
	 * implements JPEG save support, just uncomment this.
	 */

	/*
	 * TODO: Make save quality changeable (possibly with a slider on a save
	 * dialogue?)
	 */
	/*
	 * public void saveBitmapAsJPEG(String pictureName) throws
	 * FileNotFoundException { synchronized (getHolder()) { FileOutputStream fos
	 * = new FileOutputStream(pictureName);
	 * getThread().getBitmap().compress(CompressFormat.JPEG, 100, fos); } }
	 */

	public BrushPreset getCurrentPreset() {
		return mPreset;
	}

	public void setPresetColor(int color) {
		mPreset.setColor(color);
		getThread().setPreset(mPreset);
//		SimpleLog.e("", "setPresetColor --- mPreset.type == " + mPreset.type);
	}

	public void setPresetSize(float size) {
		mPreset.setSize(size);
		getThread().setPreset(mPreset);
		
//		SimpleLog.e("", "setPresetSize --- mPreset.type == " + mPreset.type);
		
	}
	
	public void setPresetType(int type) {
		mPreset.setType(type);
		getThread().setPreset(mPreset);
		
//		SimpleLog.e("", "setPresetType --- mPreset.type == " + mPreset.type);
	}

	public void setPresetBlur(Blur blurStyle, int blurRadius) {
		mPreset.setBlur(blurStyle, blurRadius);
		getThread().setPreset(mPreset);
	}

	public void setPresetBlur(int blurStyle, int blurRadius) {
		mPreset.setBlur(blurStyle, blurRadius);
		getThread().setPreset(mPreset);
	}

	public void setPreset(BrushPreset preset) {
		mPreset = preset;
		getThread().setPreset(mPreset);
	}

	public boolean isSetup() {
		return mIsSetup;
	}

	public void setup(boolean setup) {
		mIsSetup = setup;
		
		if (mIsSetup) {
			getThread().setup();
		} else {
			getThread().activate();
		}
	}
	
	public void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
	}

	public static void addActionPath(Action action) {
		if (mActionList == null) {
			mActionList = new ArrayList<Action>();
		}
//		synchronized (PainterCanvas.class) {
//		}
		mActionList.add(action);
	}
	
	public static List<Action> getActionPathList() {
//		synchronized (PainterCanvas.class) {
			if (mActionList == null) {
				mActionList = new ArrayList<Action>();
			}
//		}
		return mActionList;
	}
	
	// 鍚庨�鍓嶈繘瀹屾垚鍚庯紝缂撳瓨鐨勫姩浣�
	public static void clearSpareAction() {
		if (mActionList != null) {
			mActionList = mActionList.subList(0, mCurrentPaintIndex);
		}
	}
	
	/**
	 * 瑁佸壀瀹屽浘鐗囷紝娓呯┖action鍒楄〃
	 */
	public static void clearAllActions(){
		if (mActionList != null) {
			mActionList.clear();
			mCurrentPaintIndex = 0;
		}
	}
	
	public void recycle() {
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
			mBitmap = null;
//			SimpleLog.d(TAG, TAG + " mBitmap recycle");
		}
		if (mActionList != null) {
			mActionList = null;
			mCurrentPaintIndex = 0;
//			SimpleLog.d(TAG, TAG + " mActionList == null");
		}
	}
	
	public interface ISaveComplete {
		public void complete();
	}
	
}
