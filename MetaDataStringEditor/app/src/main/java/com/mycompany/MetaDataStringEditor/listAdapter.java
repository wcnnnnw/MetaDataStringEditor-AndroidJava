package com.mycompany.MetaDataStringEditor;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import java.io.UnsupportedEncodingException;
import java.util.List;
public class listAdapter extends  BaseAdapter  implements View.OnClickListener ,View.OnTouchListener, View.OnFocusChangeListener,View.OnLongClickListener
{
    private int selectedEditTextPosition = -1;
    @Override
    public void onFocusChange(View v, boolean p2)
    {
        EditText editText = (EditText) v;
        
        if (p2) {
            editText.addTextChangedListener(mTextWatcher);
        } else {
            editText.removeTextChangedListener(mTextWatcher);
        }
        
        
        
        
    }

    @Override
    public boolean onLongClick(View p1)
    {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {     if (event.getAction() == MotionEvent.ACTION_UP) 
    {
            EditText editText = (EditText) v;
            selectedEditTextPosition = (int) editText.getTag();
     }
        return false;
    }
    

    @Override
    public void onClick(View view)
    {
        return;
    }
    

    @Override
    public int getCount()
    {
        return b.size();
    }

    @Override
    public Object getItem(int p1)
    {
        return b.get(p1);
    }

    @Override
    public long getItemId(int p1)
    {
        return p1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup p3)
    {
        
        
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listviewitem, null);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh. listedit.setOnTouchListener(this); // 正确写法
        vh.listedit.setOnFocusChangeListener(this);
        vh.listedit.setTag(position);
        vh.listtext.setText(String.valueOf(position));
        if (selectedEditTextPosition != -1 && position == selectedEditTextPosition) { // 保证每个时刻只有一个EditText能获取到焦点
            vh.listedit.requestFocus();
        } else {
            vh.listedit.clearFocus();
        }

        try
        {
            vh.listedit.setText(new String(b.get(position), "utf-8"));
        }
        catch (UnsupportedEncodingException e)
        {}
        vh.listedit.setSelection(vh.listedit.length());

        convertView.setTag(R.id.listviewitemLinearLayout1, position); // 应该在这里让convertView绑定position
        convertView.setOnClickListener(this);
        convertView.setOnLongClickListener(this);
        return convertView;
        
        }
        
        
        
        
        
    
    
    
    

    class ViewHolder{

        
        ViewHolder(View p2){

            listedit=p2.findViewById(R.id.listviewitemEditText1);
            listtext=p2. findViewById(R.id.listviewitemTextView1);
        }
        TextView listtext;
        EditText listedit;


    }
    
    
    
    
    private TextWatcher mTextWatcher = (TextWatcher) new TextWatcher(){
    
        
        
        @Override
        public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
        {
        }

        @Override
        public void afterTextChanged(Editable p1)
        {
            b.set(selectedEditTextPosition,p1.toString().getBytes());
            Log.i("afterchanged","afterchanged");
        }
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (selectedEditTextPosition != -1) {
                Log.i("@ontextchanged","changed");
            }
        }};
    
    
    
    
    
    
    
    
    
    
    
    

        List <byte[]> b;
        private LayoutInflater mInflater;
        int ps2;
        Context context;
        listAdapter(List<byte[]> b,Context context){
            this.mInflater=LayoutInflater.from(context);
            this.b=b;
            this.context=context;
        }
        
        
    
        
        
        
        
        
        
        
    

        
        
        
        
        
        
        
        
        }
