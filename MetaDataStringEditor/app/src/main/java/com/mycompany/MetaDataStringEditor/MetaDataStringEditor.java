package com.mycompany.MetaDataStringEditor;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.channels.FileChannel;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.file.StandardOpenOption;
import java.nio.ByteOrder;
import java.util.logging.Logger;
 class MetaDataStringEditor 
{
    MetaDataStringEditor(){}
    
    MetaDataStringEditor(String inputfilename){
        
        this.inputfilename=inputfilename;
        
        
    }
    void setInputName(String inputfilename){
        this.inputfilename=inputfilename;
        
    }
//字符串地址长度类
 private class StringLiteral{
    public int Length=0;
    public int Offset=0;

    private StringLiteral(){
        this.Length=0;
        this.Offset=0;
    }
   private StringLiteral(int Length,int Offset){
        this.Length=Length;
        this.Offset=Offset;
    }
}

private String inputfilename;
    private boolean isinit=false;
    private ArrayList<StringLiteral> stringLiterals =new ArrayList<StringLiteral>();
    int version ;
    private List<byte[]> strBytes = new ArrayList<byte[]>();//字符串行列表
    private RandomAccessFile wf;
    private FileChannel file;//注意，文件通道的可读可写要建立在文件流本身可读写的基础之
    private MappedByteBuffer in;
    Long stringLiteralOffset ;      // 列表区的位置，后面不会改了
    int stringLiteralCount ;     // 列表区的大小，后面不会改了
    int DataInfoPosition ; // 记一下当前位置，后面要用
    int stringLiteralDataOffset ;  // 数据区的位置，可能要改
    int stringLiteralDataCount ;   // 数据区的长度，可能要改
  private int strlength=0;
    void init() throws IOException{
        try{file=new RandomAccessFile(inputfilename, "r").getChannel();}catch(FileNotFoundException e){throw new FileNotFoundException("File not found init failed");}
        in = file.map(FileChannel.MapMode.READ_ONLY, 0, file.size());
        in.order(ByteOrder.LITTLE_ENDIAN);//高低位切换小端顺序
        if(in.limit()==0){throw  new FileNotFoundException("File Wrong init failed");}
        if(in.getInt()==(0xFAB11BAF)){System.out.println("正确");}//头文件检查
        version = in.getInt();
        stringLiteralOffset =(long)in.getInt() ;      // 列表区的位置，后面不会改了
        stringLiteralCount =in.getInt() ;       // 列表区的大小，后面不会改了
        DataInfoPosition = in.position();  // 记一下当前位置，后面要用
        stringLiteralDataOffset = in.getInt();  // 数据区的位置，可能要改
        stringLiteralDataCount = in.getInt();   // 数据区的长度，可能要改
        /*System.out.println("stringLiteralOffset="+((stringLiteralOffset).toHexString(stringLiteralOffset)));
        System.out.println("stringLiteralCount="+((Integer)stringLiteralCount).toHexString(stringLiteralCount));
        System.out.println(Long.toHexString(DataInfoPosition));
        System.out.println("stringLiteralDataOffset="+((Integer)stringLiteralDataOffset).toHexString(stringLiteralDataOffset));
        System.out.println("stringLiteralDataCount="+((Integer)stringLiteralCount).toHexString(stringLiteralDataCount));
        System.out.println(in.position());*/
        ReadLiteral();//读取字符串数据分配
       ReadStrByte();//读取字符串
       isinit=true;
        System.out.println("___________________________________");
            
        }
    


    private void ReadLiteral() {
        in.position(stringLiteralOffset.intValue());//跳到字符数据区头部
        for (int i = 0; i < stringLiteralCount / 8; i++) {//Length + Offset
            stringLiterals.add(i,new StringLiteral(
                                   in.getInt(),
                                   in.getInt()));
        }
    }

    private void ReadStrByte()  {
        for (int i = 0; i <stringLiterals.size(); i++) {
             in.position( stringLiteralDataOffset + stringLiterals.get(i).Offset);
            
                byte[] b= new byte[stringLiterals.get(i).Length];
                in.get(b);
                strBytes.add(b);
        }
    }
    

    

    public void WriteToNewFile(String fileName) throws IOException{
        if(isinit==false){System.out.println("not init！！！"); throw new Error("NO INIT");}
        //复制文件
        wf= new RandomAccessFile(fileName, "rw");
       FileChannel out =wf.getChannel();
       out.lock();
       out.transferFrom(file,0,file.size());
       out.close();
       
        wf= new RandomAccessFile(fileName, "rw");
        out =wf.getChannel();
        out.lock();
         MappedByteBuffer  output=out.map(FileChannel.MapMode.READ_WRITE, 0, out.size());
        output.order(ByteOrder.LITTLE_ENDIAN);
        // 更新Literal
        output.position(stringLiteralOffset.intValue());
        int count = 0;
        for (int i = 0; i < stringLiterals.size(); i++) {
            stringLiterals.get(i).Offset = count;
            stringLiterals.get(i).Length = strBytes.get(i).length;
            output.putInt (stringLiterals.get(i).Length);
            output.putInt(stringLiterals.get(i).Offset);
            count += stringLiterals.get(i).Length;
        }

        // 进行一次对齐，不确定是否一定需要，但是Unity是做了，所以还是补上为好
        final int tmp = (stringLiteralDataOffset + count) % 4;
        if (tmp != 0) {count += 4 - tmp;}

        // 检查是否够空间放置
        if (count > stringLiteralDataCount) {
            // 检查数据区后面还有没有别的数据，没有就可以直接延长数据区
            if (stringLiteralDataOffset + stringLiteralDataCount < wf.length()) {
                // 原有空间不够放，也不能直接延长，所以整体挪到文件尾
                stringLiteralDataOffset =(int) wf.length();
            }
        }
        stringLiteralDataCount = count;
        // 写入string
        //       Logger.I("更新String");
        output.position(stringLiteralDataOffset);
        for (int i = 0; i < strBytes.size(); i++) {
            output.put(strBytes.get(i));
        }
        
        
        // 更新头部
        //       Logger.I("更新头部");
        output.position( DataInfoPosition);
        output.putInt(stringLiteralDataOffset);
        output.putInt(stringLiteralDataCount);

        //        Logger.I("更新完成");
        output.force();
        wf.close();
        try
        {
            finalize();
        }
        catch (Throwable e)
        {}
    }
        
     protected void finalize()throws Throwable{ 
        
        

        in.force();//刷新
        file.close();//关闭
        wf.close();//关闭
        
        
        
    }
    
    public List<byte[]> getString(){
        if (isinit==false){
            System.out.println("need init！");
            return null;
        }
        
    
    return strBytes;
    }

    
    
    

    void setstr(List<byte[]> strBytes){
        this.strBytes=strBytes;
    }
    
    
    
    boolean getisinit(){
        return isinit;
    }
  
    
    
    boolean readstr(String filename){
        

        if(isinit==false){
            System.out.println("not init");
            return false;
        }
        RandomAccessFile rf;
        List<byte[]> b=new ArrayList<byte[]>();
        try{
            rf=new RandomAccessFile(filename,"r");
            //file=wf.getChannel();
String s;
          //  in=file.map(FileChannel.MapMode.READ_ONLY,0,file.size());
            for(int i=0;(s =rf.readLine())!=null; i++){
               b.add(i,new String(s .getBytes(), "utf-8").replace("\\\\","\\").replace("\\n","\n").replace("\\r","\\r").replace("\\u000a","\u000a").replace("\\u000d","\000d").getBytes());
                
            }

            System.out.println("readstr over4");
            
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
return strBytes.size()==b.size();




    }
        
        
        
    
    
    
    
    
    
    
    
    
    void writestr(String filename) throws FileNotFoundException,IOException{
if(isinit==false){
    System.out.println("not init");
    return;
}
        RandomAccessFile wf;
        
            wf=new RandomAccessFile(filename,"rw");
          
            
            for(int i=0;i<strBytes.size();i++){
               //重置自带的回车换行
                wf.write (new String (strBytes.get(i),"utf-8").replace("\\","\\\\").replace("\n","\\n").replace("\r","\\r").replace("\u000a","\\u000a").replace("\u000d","\\000d").getBytes());
                if (i!=strBytes.size()){
                wf.writeChar('\n');}

            

            System.out.println("writestr over3");
        
        }
        
        }
        
        int getstrlength(){
            
            for(int i=0;i<strBytes.size();i++){
                
                strlength+=strBytes.get(i).length;
                
                
            }
            
            return strlength;
        
        
        
       
        
        
    }
    
    
    
    
    
    
    
    
    
    
    }
    
