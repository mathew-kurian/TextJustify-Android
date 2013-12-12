package mk.justifytext;

import android.graphics.Paint;
import android.view.Gravity;
import android.widget.TextView;

public class TextJustify { 

final static String SYSTEM_NEWLINE  = "\n";
final static float COMPLEXITY = 5.12f;  //Reducing this will increase efficiency but will decrease effectiveness
final static Paint p = new Paint();

/* @author Mathew Kurian */

public static void run(final TextView tv, float origWidth, int paddingLeft, int paddingRight, int marginLeft, int marginRight) {
		
		
    origWidth-= paddingRight+marginRight+paddingLeft+marginLeft;
    String s = tv.getText().toString();
    p.setTypeface(tv.getTypeface());        
    String [] splits = s.split(SYSTEM_NEWLINE);
    float width = origWidth - 5;
    for(int x = 0; x<splits.length;x++)
        if(p.measureText(splits[x])>width){
            splits[x] = wrap(splits[x], width, p);
            String [] microSplits = splits[x].split(SYSTEM_NEWLINE);
            for(int y = 0; y<microSplits.length-1;y++)
                microSplits[y] = justify(removeLast(microSplits[y], " "), width, p);
            StringBuilder smb_internal = new StringBuilder();
            for(int z = 0; z<microSplits.length;z++)
                smb_internal.append(microSplits[z]+((z+1<microSplits.length) ? SYSTEM_NEWLINE : ""));
            splits[x] = smb_internal.toString();
        }       
    final StringBuilder smb = new StringBuilder();
    for(String cleaned : splits)
        smb.append(cleaned+SYSTEM_NEWLINE);
    tv.setGravity(Gravity.LEFT);
    tv.setText(smb);
}
private static String wrap(String s, float width, Paint p){
    String [] str = s.split("\\s"); //regex
    StringBuilder smb = new StringBuilder(); //save memory
    smb.append(SYSTEM_NEWLINE);
    for(int x = 0; x<str.length; x++){
        float length = p.measureText(str[x]);
        String [] pieces = smb.toString().split(SYSTEM_NEWLINE);
        try{
            if(p.measureText(pieces[pieces.length-1])+length>width)         
                smb.append(SYSTEM_NEWLINE);
        }catch(Exception e){}
        smb.append(str[x] + " ");
    }
    return smb.toString().replaceFirst(SYSTEM_NEWLINE, "");
}
private static String removeLast(String s, String g){
    if(s.contains(g)){
        int index = s.lastIndexOf(g);
        int indexEnd = index + g.length();
        if(index == 0) return s.substring(1);
        else if(index == s.length()-1)  return s.substring(0, index);
        else
            return s.substring(0, index) + s.substring(indexEnd);
    }
    return s;
}
private static String justifyOperation(String s, float width, Paint p){
    float holder = (float) (COMPLEXITY*Math.random());
    while(s.contains(Float.toString(holder)))
        holder = (float) (COMPLEXITY*Math.random());
    String holder_string = Float.toString(holder);
    float lessThan = width;
    int timeOut = 100;
    int current = 0;
    while(p.measureText(s)<lessThan&&current<timeOut) {
        s = s.replaceFirst(" ([^"+holder_string+"])", " "+holder_string+"$1");
        lessThan = p.measureText(holder_string)+lessThan-p.measureText(" ");
        current++;          
    }
    String cleaned = s.replaceAll(holder_string, " ");
    return cleaned;
}
private static String justify(String s, float width, Paint p){
    while(p.measureText(s)<width){
        s = justifyOperation(s,width, p);
    }
    return s;
}
