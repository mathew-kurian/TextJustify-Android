package mk.justifytext;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.TextView;

/* @author Mathew Kurian */

public class TextViewEx extends TextView 
{		
	private Paint p = new Paint();
	private String [] blocks;
	
	private float spaceOffset = 0;
	private float horizontalOffset = 0;
	private float verticalOffset = 0;
	private float horizontalFontOffset = 0;
	private float textWrapWidth = 0;
	
	private String wrappedText;
	
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

	public void setWrappedText(String st)
	{
		wrappedText = st;
	}
	
	public String getWrappedText()
	{
		return wrappedText;
	}
	
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		wrappedText = getText().toString();		
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{		
		if(wrappedText == null)
		{
			// Set the intial wrapped text
			setWrappedText(getText().toString());
		}
		
		/************************************************************************
		## Debugging handle.
		**************************************************************************/ 
		// super.onDraw(canvas);
		
		p.setStyle(Style.STROKE);
		p.setColor(getCurrentTextColor());
		p.setTypeface(getTypeface());
		p.setTextSize(getTextSize());
		
		textWrapWidth = getWidth();
		verticalOffset = horizontalFontOffset = getLineHeight() - 1.0f; // Temporary fix for overflow issue
		spaceOffset = p.measureText(" ");
		blocks = getWrappedText().split("(?<=\n)");
		
		for(int i = 0; i < blocks.length; i++)
		{
			String block = blocks[i];
			
			if(block.equals("\n") && (i + 1 < blocks.length ? blocks[i + 1].equals("\n"): false))
			{
				verticalOffset += horizontalFontOffset; 
				continue;
			}
			
			block = block.trim();
			
			if(block.length() == 0) continue;
			
			Object [] wrapped = wrap(block, textWrapWidth, p);
			boolean wrap = (Float) wrapped[1] != Float.MIN_VALUE;
			float extraSpace = (Float) wrapped[1];
			String wrappedLine = ((String) wrapped[0]);
			String [] words = wrappedLine.split(" ");
			
			for(String word : words)
			{
				canvas.drawText(word, horizontalOffset, verticalOffset, p);
				horizontalOffset += p.measureText(word) + spaceOffset + (wrap ? extraSpace/(words.length - 1) : 0);
			}

			horizontalOffset = 0;
			verticalOffset += horizontalFontOffset; 
			
			if(blocks[i].length() > 0)
			{
				blocks[i] = blocks[i].substring(wrappedLine.length());
				i--;
			}
		}
	}

	private Object [] wrap(String s, float width, Paint p)
	{
		float cacheWidth = width;
		
		if(p.measureText(s) <= width)
		{
			return new Object[] { s, Float.MIN_VALUE };
		}

		StringBuilder smb = new StringBuilder();

		for(String word : s.split("\\s"))
		{
			cacheWidth = p.measureText(word);
			width -= cacheWidth;
			
			if(width <= 0)
			{
				return new Object[] { smb.toString(), width + cacheWidth + spaceOffset };
			}

			smb.append(word);
			smb.append(" ");
			width -= spaceOffset;
		}
		
		return new Object[] { smb.toString(), width };
	}
}