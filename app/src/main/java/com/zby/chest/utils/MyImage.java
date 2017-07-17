package com.zby.chest.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.zby.chest.utils.MyImage.ScalingLogic;

import java.io.InputStream;

public class MyImage {
	public static class ScalingLogic {

		public static final ScalingLogic FIT = new ScalingLogic();

	}

	// 图片解码 或者 提高像素
	public static Bitmap decodeFile(String pathName, int dstWidth,
			int dstHeight, ScalingLogic scalingLogic) {

		Options options = new Options();

		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(pathName, options);

		options.inJustDecodeBounds = false;

		options.inSampleSize = calculateSampleSize(options.outWidth,
				options.outHeight, dstWidth, dstHeight, scalingLogic);

		Bitmap unscaledBitmap = BitmapFactory.decodeFile(pathName, options);
		
		return unscaledBitmap;

	}
	
	  public static Bitmap decodeResources(Context mContext,
              int srcId, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
          // TODO Auto-generated method stub
	      Options options = new Options();

	        options.inJustDecodeBounds = true;

	        BitmapFactory.decodeResource(mContext.getResources(), srcId, options);

	        options.inJustDecodeBounds = false;

	        options.inSampleSize = calculateSampleSize(options.outWidth,
	                options.outHeight, dstWidth, dstHeight, scalingLogic);
	        
	        //options.inPreferredConfig = Bitmap.Config.RGB_565;

	        Bitmap unscaledBitmap = BitmapFactory.decodeResource(mContext.getResources(), srcId, options);
	        
	        return unscaledBitmap;
      }   

	private static int calculateSampleSize(int srcWidth, int srcHeight,
			int dstWidth, int dstHeight, ScalingLogic scalingLogic) {

		if (scalingLogic == ScalingLogic.FIT) {

			final float srcAspect = (float) srcWidth / (float) srcHeight;

			final float dstAspect = (float) dstWidth / (float) dstHeight;

			if (srcAspect > dstAspect) {
				return srcWidth / dstWidth;
			} else {
				return srcHeight / dstHeight;
			}
		} else {
			final float srcAspect = (float) srcWidth / (float) srcHeight;
			final float dstAspect = (float) dstWidth / (float) dstHeight;
			if (srcAspect > dstAspect) {
				return srcHeight / dstHeight;
			} else {
				return srcWidth / dstWidth;
			}

		}

	}

	// 缩放图片
	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		if (bitmap == null) {
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleWidht);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		if (bitmap.isRecycled()) {
			bitmap.recycle();
		}
		if (newbmp.isRecycled()) {
			newbmp.recycle();
		}
		return newbmp;
	}

	/**
	 * 旋转图片
	 * 
	 * @param bmpOrg
	 * @param rotate
	 * @return
	 */
	public static Bitmap getRotateBitmap(
			Bitmap bmpOrg, int rotate) {
		// 获取图片的原始的大小
		int width = bmpOrg.getWidth();
		int height = bmpOrg.getHeight();

		//int newWidth = 300;
		//int newheight = 300;
		// 定义缩放的高和宽的比例
		//float sw = ((float) newWidth) / width;
		//float sh = ((float) newheight) / height;
		// 创建操作图片的用的Matrix对象
		android.graphics.Matrix matrix = new android.graphics.Matrix();
		// 缩放翻转图片的动作
		// sw sh的绝对值为绽放宽高的比例，sw为负数表示X方向翻转，sh为负数表示Y方向翻转
		//matrix.postScale(sw, sh);
		// 旋转30*
		matrix.postRotate(rotate);
		// 创建一个新的图片
		android.graphics.Bitmap resizeBitmap = android.graphics.Bitmap
				.createBitmap(bmpOrg, 0, 0, width, height, matrix, true);
		return resizeBitmap;
	}

	// 将Drawable转化为Bitmap
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable == null) {
			return null;
		}
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;

	}

	// 获得圆角图片的方法
	public static Bitmap getRoundedCornerBitmap(String path, float roundPx) {
		Bitmap bitmap = getBitmap2SDK(path);
		return getRoundedCornerBitmap(bitmap, roundPx);
	}
	

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		if (bitmap == null) {
			return null;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	// 获得带倒影的图片方法
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		final int reflectionGap = 4;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
				width, height / 2, matrix, false);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height / 2), Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);

		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
				0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);

		return bitmapWithReflection;
	}

	// 从SDK卡上获取图片
	public static Bitmap getBitmap2SDK(String path) {
		try {

			if (path == null) {
				return null;
			}

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, opts);

			opts.inSampleSize = computeSampleSize(opts, -1, 128 * 128);
			opts.inJustDecodeBounds = false;
			try {
				Bitmap bmp = BitmapFactory.decodeFile(path, opts);
				return bmp;
			} catch (OutOfMemoryError err) {
			}
		} catch (Exception e) {
			return null;
		}
		return null;

	}

	private static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
	
	
	
	// 图片解码 或者 提高像素
		public static BitmapDrawable decodeFileBitmapDrawable(Resources  res,int id, int dstWidth,
				int dstHeight, ScalingLogic scalingLogic) {

			Options options = new Options();

			options.inJustDecodeBounds = true;


			options.inJustDecodeBounds = false;

			options.inSampleSize = calculateSampleSize(options.outWidth,
					options.outHeight, dstWidth, dstHeight, scalingLogic);

			BitmapDrawable unscaledBitmap = new BitmapDrawable(BitmapFactory.decodeResource(res, id, options));
			
			return unscaledBitmap;
		}
		
		/** 
		 * 以最省内存的方式读取本地资源的图片 
		 * @param context 
		 * @param resId 
		 * @return 
		 */  
		public static Bitmap readBitMap(Context context, int resId){  
		    BitmapFactory.Options opt = new BitmapFactory.Options();  
		    opt.inPreferredConfig = Bitmap.Config.RGB_565;   
		    opt.inPurgeable = true;  
		    opt.inInputShareable = true;  
		       //获取资源图片  
		    InputStream is = context.getResources().openRawResource(resId);  
		        return BitmapFactory.decodeStream(is,null,opt);  
		}


}
