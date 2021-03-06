package com.mycompany.MetaDataStringEditor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import android.view.Menu;
import android.support.v7.widget.Toolbar;
import java.io.RandomAccessFile;
import android.content.DialogInterface;
import java.io.FileNotFoundException;
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.text.TextUtils;


public class MainActivity extends AppCompatActivity
{
    LinearLayout mainactivity1layout;
    EditText inputfileedit;
    EditText outputfileedit;
    boolean  isclick=true;
    ListView listview;
    List <byte[]> b;
    MetaDataStringEditor m;
    String inputfile;
    String outputfile;
    Toolbar toolbar;
    String stroutput;
    EditText searchedit;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainactivity1layout = findViewById(R.id.activitymainLinearLayout1);
        inputfileedit = findViewById(R.id.inputEditText1);
        outputfileedit = findViewById(R.id.outputEditText2);
        listview = findViewById(R.id.activitymainListView1);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
       searchedit=findViewById(R.id.activitymainsearch);
        searchedit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH){
                        if (TextUtils.isEmpty(searchedit.getText().toString().trim())){
                            Toast.makeText(MainActivity.this, "???????????????", Toast.LENGTH_SHORT).show();
                            return true;
                        }else {
                            //??????
                            for(int i=0;i<b.size();i++){
                                try
                                {
                                    if(new String(b.get(i), "utf-8").contains(searchedit.getText().toString()))
                                    {
                                        
                                        listview.setSelection(i);
                                        return false;
                                        
                                    }
                                }
                                catch (UnsupportedEncodingException e)
                                {
                                    e.printStackTrace();
                                    return false;
                                    
                                }}

                            
                        }
                    }
                    return false;
                }
            });

    }







//????????????
    public void startbutton(View view)
    {
            onstartbuttonclick(getApplication());
        
    }

    
    
    void onstartbuttonclick(Context context) 
    {
        if (isclick == false)
        {return;}
        outputfile=outputfileedit.getText().toString();
        File f=new File(inputfileedit.getText().toString());
        if (f.exists())
        {
            searchedit.setVisibility(searchedit.VISIBLE);
            mainactivity1layout.setVisibility(LinearLayout.GONE);
            inputfile = inputfileedit.getText().toString();
            outputfile = outputfileedit.getText().toString();
            m = new MetaDataStringEditor(inputfile);
            try
            {

                m.init();
                b = m.getString();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getMessage() , Toast.LENGTH_LONG).show();

                return ;
            }
            System.out.println( 
                b.size());
            listAdapter adapter=new listAdapter(
                b, context);
            listview.setAdapter(adapter);
            isclick = false;
        }
        else
        {Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();}



    }



    
    //????????????global-metadata.dat

    public void write(View view)
    {
        if(m==null||!m.getisinit()){ Toast.makeText(getApplicationContext(),"?????????????????????",Toast.LENGTH_SHORT).show();
            return;
        }
        if( outputfile!=""|outputfile!=null|outputfile!=" "){

            try
            {
                m.WriteToNewFile(outputfile);

                Toast.makeText(getApplicationContext(),"????????????" ,Toast.LENGTH_LONG ).show();
                return;
            }
            catch (IOException e)
            {
                Toast.makeText(getApplicationContext(),"????????????????????????"+"\n"+e.getMessage() ,Toast.LENGTH_LONG ).show();
                showWriteDialog();
            }
        }
        else{
        showWriteDialog();}
    }

    
    
    
    //???????????????
    public void export(View view)
    {
        if(m==null||m.getisinit()==false)
        {
            Toast.makeText(getApplicationContext(),"??????????????????",Toast.LENGTH_SHORT).show();
            return;
        }
        showExportDialog("msg");
    }

    
    
    

//???????????????
public void importstr(View v){
    
    
    showImportDialog();
    
    
}




private void showWriteDialog(){
    


    // ??????EditText
    final EditText editText = new EditText(MainActivity.this);
    editText.setSingleLine();
    editText.setHint("?????????");
    editText.requestFocus();
    editText.setFocusable(true);
    AlertDialog.Builder inputDialog = new AlertDialog.Builder(this)
        .setTitle("?????????").setMessage("global-metadata.dat")
        .setView(editText).setPositiveButton("??????",
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // TODO ???????????????
                if(m==null||m.getisinit()==false)
                {
                  Toast.makeText(getApplicationContext(),"??????????????????",Toast.LENGTH_SHORT ).show();
                  return;
                }
                String globalMetadataFileName=editText.getText().toString();
                
                try
                {
                    m.WriteToNewFile(globalMetadataFileName);

                    Toast.makeText(getApplicationContext(),"????????????" ,Toast.LENGTH_LONG ).show();
                }
                catch (IOException e)
                {
                    Toast.makeText(getApplicationContext(),"????????????"+"\n"+e.getMessage() ,Toast.LENGTH_LONG ).show();
                    
                }

            }
            
        }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.dismiss();
            }
        });
    inputDialog.create().show();
    
    
    
    
}









private void showImportDialog()
{
    // ??????EditText
    if(m==null||m.getisinit()==false)
    {
        Toast.makeText(getApplicationContext(),"??????????????????",Toast.LENGTH_LONG ).show();
        return;
    }
    final EditText editText = new EditText(MainActivity.this);
    editText.setSingleLine();
    editText.setHint("????????????????????????");
    editText.requestFocus();
    editText.setFocusable(true);
    AlertDialog.Builder inputDialog = new AlertDialog.Builder(this)
        .setTitle("?????????").setMessage("?????????????????????????????????????????????????????????\"\\n\"??????????????????????????????????????????\"\\\"??????2??????????????????????????????????????????????????????????????????????????????????????????????????????")
        .setView(editText).setPositiveButton("??????",
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                
                // TODO ???????????????
                if (m.getisinit() == false)
                {

                    Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                String strfilename=editText.getText().toString();
                if(!m.readstr(strfilename))
                {
                    Toast.makeText(getApplicationContext(),"?????????????????????",Toast.LENGTH_SHORT).show();
                }
                
            }
        }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.dismiss();
            }
        });
    inputDialog.create().show();
}

    
    













    private String showExportDialog(String msg)
    {
        // ??????EditText
        final EditText editText = new EditText(MainActivity.this);
        editText.setSingleLine();
        editText.setHint("????????????????????????");
        editText.requestFocus();
        editText.setFocusable(true);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(this)
            .setTitle("?????????").setMessage("???????????????????????????????????????????????????\"\\n\"?????????\"\\r\"??????\"\\u000a\"???\"\\u000d\"?????????????????????????????????\" \\  \\\\???????????????????????? \"??????????????????????????????????????? @?????? $?????? %?????? ????????? ?????????")
            .setView(editText).setPositiveButton("??????",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    // TODO ???????????????
                    if (m.getisinit() == false)
                    {

                        Toast.makeText(getApplicationContext(), "?????????????????????", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try
                    {
                    
                   
                        
                        m.writestr(editText.getText().toString());
                        
                        
                        
                    }
                    catch (IOException e )
                    {
                        Toast.makeText(getApplicationContext(), "????????????"+e.getMessage(), Toast.LENGTH_LONG).show();
                        return;}
                    
                    Toast.makeText(getApplicationContext(), "??????", Toast.LENGTH_LONG).show();
                }
            }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    dialogInterface.dismiss();
                }
            });
        inputDialog.create().show();
        return editText.getText().toString();
    }

    @Override
    protected void onResume()
    {
        
       if (mainactivity1layout.getVisibility()!=mainactivity1layout.GONE){
         //  mainactivity1layout.setVisibility(mainactivity1layout.GONE);
       }
        
        super.onResume();
    }
    
    
    
    
    
    

}


    
