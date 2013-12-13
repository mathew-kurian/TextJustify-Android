
package com.example.textjustify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

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
	private boolean wrapEnabled = false;
	
	private float strecthOffset;
	private float wrappedEdgeSpace;
	private String block;
	private String wrappedLine;
	private String [] lineAsWords;
	private Object [] wrappedObj;
	
	public TextViewEx(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}

	public TextViewEx(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}

	public TextViewEx(Context context) 
	{
		super(context);
	}

	public void setText(String st, boolean wrap)
	{
		wrapEnabled = wrap;	
		
		super.setText(st);
	}

	@Override
	protected void onDraw(Canvas canvas) 
	{
		if(!wrapEnabled)
		{
			super.onDraw(canvas);
			return;
		}

		// Pull widget properties
		paint.setColor(getCurrentTextColor());
		paint.setTypeface(getTypeface());
		paint.setTextSize(getTextSize());
				
		dirtyRegionWidth = getWidth();
		blocks = getText().toString().split("((?<=\n)|(?=\n))");
		verticalOffset = horizontalFontOffset = getLineHeight();
		spaceOffset = paint.measureText(" ");
		
		for(int i = 0; i < blocks.length; i++)
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
			
			for(String word : lineAsWords)
			{
				canvas.drawText(word, horizontalOffset, verticalOffset, paint);
				horizontalOffset += paint.measureText(word) + spaceOffset + strecthOffset;
			}
			
			if(blocks[i].length() > 0)
			{
				blocks[i] = blocks[i].substring(wrappedLine.length());				
				verticalOffset += blocks[i].length() > 0 ? horizontalFontOffset : 0; 				
				i--;
			}
		}
	}
}