package com.example.textjustify;

import com.fscz.util.TextJustifyUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.TextView;
import android.util.AttributeSet;

/*
 * 
 * TextViewEx.java
 * @author Mathew Kurian
 * 
 * !-- Requires -- !
 * TextJustifyUtils.java
 * 
 * From TextJustify-Android Library v1.0.2
 * https://github.com/bluejamesbond/TextJustify-Android
 *
 * Please report any issues
 * https://github.com/bluejamesbond/TextJustify-Android/issues
 * 
 * Date: 12/13/2013 12:28:16 PM
 * 
 */

public class TextViewEx extends TextView 
{		
	private Paint paint = new Paint();

	private String [] blocks;
	private float spaceOffset = 0;
	private float horizontalOffset = 0;
	private float verticalOffset = 0;
	private float horizontalFontOffset = 0;
	private float dirtyRegionWidth = 0;
	
	private float strecthOffset;
	private float wrappedEdgeSpace;
	private String block;
	private String wrappedLine;
	private String [] lineAsWords;
	private Object[] wrappedObj;
	
	private Bitmap cache = null;
	private boolean cacheEnabled = false;
	
	public TextViewEx(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}

	public TextViewJustify(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}

	public TextViewJustify(Context context) 
	{
		super(context);
	}
	
	@Override
	public void setDrawingCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}

	@Override
	protected void onDraw(Canvas canvas) 
	{
		Canvas newCanvas = null;
		if (cacheEnabled) {

			if (cache != null) {
				canvas.drawBitmap(cache, 0, 0, paint);
				return;
			} else  {
				cache = Bitmap.createBitmap(getWidth(), getHeight(), 
	                    Config.ARGB_4444);
				newCanvas = new Canvas(cache);
			}
		} else {
			newCanvas = canvas;
		}
		
		// Pull widget properties
		paint.setColor(getCurrentTextColor());
		paint.setTypeface(getTypeface());
		paint.setTextSize(getTextSize());
				
		dirtyRegionWidth = getWidth();
		int maxLines = getMaxLines();
		blocks = getText().toString().split("((?<=\n)|(?=\n))");
		verticalOffset = horizontalFontOffset = getLineHeight() - 0.5f; // Temp fix
		spaceOffset = paint.measureText(" ");
		
		int lines = 1;
		for(int i = 0; i < blocks.length && lines <= maxLines; i++)
		{
			block = blocks[i];
			horizontalOffset = 0;

			if(block.length() == 0)
			{
				continue;
			}			
			else if(block.equals("\n"))
			{
				verticalOffset += horizontalFontOffset; 
				continue;
			}
			
			block = block.trim();
			
			if(block.length() == 0) continue;
			
			wrappedObj = TextJustifyUtils.createWrappedLine(block, paint, spaceOffset, dirtyRegionWidth);
			
			wrappedLine = ((String) wrappedObj[0]);
			wrappedEdgeSpace = (Float) wrappedObj[1];
			lineAsWords = wrappedLine.split(" ");
			strecthOffset = wrappedEdgeSpace != Float.MIN_VALUE ? wrappedEdgeSpace/(lineAsWords.length - 1) : 0;
			
			for(int j = 0; j < lineAsWords.length; j++)
			{
				String word = lineAsWords[j];
				if (lines == maxLines && j == lineAsWords.length - 1) {
					newCanvas.drawText("...", horizontalOffset, verticalOffset, paint);
				} else {
					newCanvas.drawText(word, horizontalOffset, verticalOffset, paint);
				}
				horizontalOffset += paint.measureText(word) + spaceOffset + strecthOffset;
			}
			lines++;
			
			if(blocks[i].length() > 0)
			{
				blocks[i] = blocks[i].substring(wrappedLine.length());				
				verticalOffset += blocks[i].length() > 0 ? horizontalFontOffset : 0; 				
				i--;
			}
		}
		
		if (cacheEnabled) {
			canvas.drawBitmap(cache, 0, 0, paint);
		}
	}
}
