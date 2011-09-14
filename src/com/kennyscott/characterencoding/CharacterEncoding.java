package com.kennyscott.characterencoding;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class CharacterEncoding {

	public static void main( String[] args ) {
		try {
			new CharacterEncoding().execute( args );
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	private void execute( String[] args ) throws Exception {
		Charset charset = Charset.forName( "ISO-8859-1" );
		CharsetDecoder decoder = charset.newDecoder();
		ByteBuffer byteBuffer = ByteBuffer.wrap( new byte[]{ (byte) 0x54, (byte) 0xE2, (byte) 0x20 } );

		CharBuffer charBuffer = decoder.decode( byteBuffer );
		System.out.println( "Character: [" + charBuffer + "]" );

		byteBuffer.flip();

		FileChannel fileChannel = new FileOutputStream( "data2.txt" ).getChannel();
		fileChannel.write( byteBuffer );
		fileChannel.close();

		byte[] utfBytes = charBuffer.toString().getBytes( "UTF-8" );
		FileOutputStream fos = new FileOutputStream( "data3.txt" );
		fos.write( utfBytes );
		fos.close();

		byte[] moreBytes = charBuffer.toString().getBytes( "ISO-8859-11" );
		fos = new FileOutputStream( "data4.txt" );
		fos.write( moreBytes );
		fos.close();
	}

}
