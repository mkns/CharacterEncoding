
package com.kennyscott.characterencoding;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * This attempts to help me understand how Java encodes characters. The character which is used to
 * identify this is 0xE2, which is a Latin-1 (ISO-8859-1) character. This gets converted to Unicode,
 * and when you look at data3.txt after running this script, you'll see that the raw bytes are
 * different. The neat thing is that the characters in the file are the same, so long as you are
 * looking at them using a system (e.g. terminal) which is using the right encoding. So, for
 * data2.txt, you need a Latin-1 (ISO-8859-1) encoded terminal, whereas for data3.txt, you want a
 * UTF-8 terminal. If you compare the characters this way, they are the same.
 * 
 * @author mkns
 * 
 */
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
