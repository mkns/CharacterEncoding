
package com.kennyscott.characterencoding;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.commons.lang3.ArrayUtils;

/*
 * Some vague notes as I attempt to work shit out.
 * 
 * Single byte characters in UTF-8 start with a 0, always.
 * 
 * Multiple byte characters in UTF-8 start with 11xxxxxx. Therefore, in hex, the first byte must
 * either be 7F (01111111) or below, which means it's a single byte character, or C0 or greater, in
 * which case it's a multiple byte character. If the first byte is between 80 and BF inclusive,
 * something's gone wrong, most likely you're receiving something that isn't UTF-8.
 * 
 * The continuation bytes (bytes 2 thru 6) start with 10, therefore the hex representation of those
 * continuation characters must be between 80 and BF, because 80 is 10000000 and BF is 10111111.
 * 
 * Using these notes, let's check with the examples below. First, I tried a cent. The first byte is
 * C2, which is above C0 and thus a multi-byte character. Win. The second byte, which is therefore a
 * continuation byte, is A2, which is between 80 and BF. Holy shit, this might all work out.
 * 
 * Next, let's look at the euro. It's a three byte character. First byte is E2 - that's above C0.
 * Good. Next two bytes need to be between 80 and BF, and what were they? 82 and AC. Holy fucking
 * shit, this seems to be working. Or I'm just plain lucky.
 * 
 * Finally, I added a 4 byte character, and to be fair, I have no fucking idea what it is, or what
 * it's supposed to look like. However, first byte is is above C0, as it's F0. The others are
 * between 80 and BF, as they are 91, 82 and 80. Sweeeet.
 * 
 * --------------------------------------------------------------------------------------------------
 * 
 * Taking it to the next ludicrously low level, you need to read these docs:
 * 
 * http://en.wikipedia.org/wiki/Utf-8
 * 
 * http://www.unicode.org/Public/UNIDATA/NamesList.txt
 * 
 * And a useful hex / binary / decimal converter:
 * 
 * http://easycalculation.com/hex-converter.php
 * 
 * That unicode nameslist shows the code points for the characters. The euro sign, for example, is
 * 20AC. This looks different to E282AC, which we previously worked out was the UTF-8 character for
 * the euro. It's because 20AC is the hex representation of the remaining bits in the bytes after
 * you've ignored the bits that are telling you, for example, how many bytes make up the character.
 * So, in binary, the euro is:
 * 
 * 11100010 10000010 10101100
 * 
 * That's equal to E2 82 AC (honest). Now, we know that a 3 byte UTF-8 character will be like this:
 * 
 * 1110xxxx 10xxxxxx 10xxxxxx
 * 
 * Note that the starting characters for each of those three blocks do actually match the binary
 * representation of the euro sign. Now, count up those x characters, and there are 16 of them.
 * Extract the numbers from the binary version of the euro (as in, looking at the positions of the x
 * characters, take those numbers out)...
 * 
 * 0010 000010 101100
 * 
 * ...convert them from blocks of 4, 6 and 6 to two blocks of 8:
 * 
 * 00100000 10101100
 * 
 * And what does that make?
 * 
 * 20 AC
 * 
 * Which just happens to be the code point of the euro sign. No, I'm not shitting you. This actually
 * works.
 * 
 * This comment is wildly longer than the code. Hey ho, that's pretty normal for me, tbf.
 */
public class Utf8Encoding {

	public static void main( String[] args ) {
		try {
			new Utf8Encoding().execute( args );
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	private void execute( String[] args ) throws Exception {
		byte[] bytes = new byte[0];

		byte[] cent = new byte[]{ (byte) 0xC2, (byte) 0xA2 };
		bytes = (byte[]) ArrayUtils.addAll( bytes, cent );

		byte[] space = new byte[]{ (byte) 0x20 };
		bytes = (byte[]) ArrayUtils.addAll( bytes, space );

		byte[] euro = new byte[]{ (byte) 0xE2, (byte) 0x82, (byte) 0xAC };
		bytes = (byte[]) ArrayUtils.addAll( bytes, euro );

		bytes = (byte[]) ArrayUtils.addAll( bytes, space );

		byte[] candrabindu = new byte[]{ (byte) 0xf0, (byte) 0x91, (byte) 0x82, (byte) 0x80 };
		bytes = (byte[]) ArrayUtils.addAll( bytes, candrabindu );

		bytes = (byte[]) ArrayUtils.addAll( bytes, space );
		bytes = (byte[]) ArrayUtils.addAll( bytes, space );

		ByteBuffer byteBuffer = ByteBuffer.wrap( bytes );

		CharsetDecoder decoder = Charset.forName( "UTF-8" ).newDecoder();
		CharBuffer charBuffer = decoder.decode( byteBuffer );
		System.out.println( "Character: [" + charBuffer + "]" );

		byteBuffer.flip();

		byte[] utfBytes = charBuffer.toString().getBytes( "UTF-8" );
		FileOutputStream fos = new FileOutputStream( "utf8.txt" );
		fos.write( utfBytes );
		fos.close();
	}

}
