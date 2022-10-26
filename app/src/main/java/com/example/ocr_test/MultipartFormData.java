package com.example.ocr_test;

import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MultipartFormData {
    private static class DataItem {
        String name;
        byte[] data;     // 1(String) : 미리 변환된 데이터, 2(byte)일경우 입력데이터의 복사본, 3(memory file)일경우 file data
        String fileName; // 전송될 파일이름
        String filePath; // local상의 파일이름 // 4
    }

    private List<DataItem> mList = new ArrayList< DataItem >();

    // 1. java를 쓸 것이기 때문에 String 데이터 전송
    public void addString( String name, String value, String charset ) throws UnsupportedEncodingException {

        DataItem d = new DataItem();

        d.name = name;
        d.data = value.getBytes( charset );

        mList.add( d );
    }
    // 2. binary데이터 전송
    public void addBinary( String name, byte[] value ) {

        DataItem d = new DataItem();

        d.name = name;
        if( value != null ) {
            d.data = new byte[value.length];
            System.arraycopy(value, 0, d.data, 0, value.length);
        }

        mList.add( d );
    }
    // 3. 파일로 binary전송 : 실제 데이터가 메모리상에 있고 이 것을 서버에서 파일로 인식하도록 전송
    public void addFile( String name, String filename, byte[] value ) {

        DataItem d = new DataItem();

        d.name = name;
        d.fileName = filename;
        if( value != null ) {
            d.data = new byte[value.length];
            System.arraycopy(value, 0, d.data, 0, value.length);
        }
    }
    // 4. local 파일로 데이터전송 : 데이터가 파일로 있는 경우
    public void addFile( String name, String filename ) {

        DataItem d = new DataItem();
        d.name = name;
        d.fileName = new File( filename ).getName();
        d.filePath = filename;

    }

    public int computeContentLength() {
        int ret = 0;
        // ...
        return ret;
    }

    public void Write( OutputStream out ) {
        // ...
    }
}
