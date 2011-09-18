
package com.kennyscott.characterencoding;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

/**
 * The point of this is to show that an XML document with the wrong charset in the declaration could
 * explode things if you have characters outwith the ASCII range.
 * 
 * What this class does is build a simple XML document with two elements, the first with basic text
 * and the second with the 'cent' character. The same routine is run four times, with two different
 * charsets in the declaration and converting the bytes using different charsets.
 * 
 * If the declaration is the same as the charset used to break up the String into raw bytes,
 * everything works fine.
 * 
 * Things go odd when the declaration is ISO-8859-1 but the data is broken down using UTF-8. Here,
 * the cent character is transformed to two bytes, 'c2' and 'a2', since in UTF-8, the cent character
 * is a double byte character. However, when that is then read using ISO-8859-1, which is single
 * byte characters only, it is read therefore as two separate characters; 'a-circumflex' is c2, and
 * 'cent' is a2. Confusion therefore might be evident if the user doesn't know what is going on,
 * because the 'cent' is displayed, but the 'a-circumflex' is also displayed first, apparently in
 * error. The error is actually that the String was broken into bytes of the wrong character set.
 * 
 * Things crash during XML parsing when the declaration is UTF-8 and the data is broken down using
 * ISO-8859-1. Here, the byte written using the character set ISO-8859-1 is a2, but this is an
 * invalid first byte of a UTF-8 character, irrespective of whether it's a single byte character or
 * multi-byte. Single byte characters in UTF-8 must be 7F or below, and the first byte of a
 * multi-byte character must be C0 or above. A2 is neither, therefore parsing correctly explodes.
 * Again, the fault is because the data was broken down to ISO-8859-1 bytes, but read using
 * something else; UTF-8, in this example.
 * 
 * @author mkns
 * 
 */
public class XmlParsing {

	public static void main( String[] args ) {
		try {
			new XmlParsing().execute( args );
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	private void execute( String[] args ) throws Exception {
		execute( "ISO-8859-1" );
		execute( "UTF-8" );
		execute( "ISO-8859-1", "UTF-8" );
		execute( "UTF-8", "ISO-8859-1" );
	}

	private void execute( String charset ) throws Exception {
		execute( charset, charset );
	}

	private void execute( String charset, String convertToCharset ) throws Exception {
		log( "Executing using charset " + charset + ", converting to charset " + convertToCharset );
		String xml = getData( charset );

		writeToFile( xml.toString(), charset );

		Document document = null;

		try {
			document = parseXmlFile( new ByteArrayInputStream( xml.getBytes( convertToCharset ) ) );
		}
		catch ( SAXParseException e ) {
			log( "Parsing using charset " + charset + " has crashed." );
			System.exit( 1 );
		}
		Element root = document.getDocumentElement();

		NodeList firstElements = root.getElementsByTagName( "first" );
		Element first = (Element) firstElements.item( 0 );
		log( "First element: " + first.getTextContent() );

		NodeList secondElements = root.getElementsByTagName( "second" );
		Element second = (Element) secondElements.item( 0 );
		log( "Second element: " + second.getTextContent() );
	}

	private Document parseXmlFile( InputStream is ) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse( is );
	}

	/**
	 * This method generates the XML data. Note that the charset used to generate the document is
	 * largely irrelevant; I used UTF-8 and thus generated the 'cent' character using two bytes (C2
	 * and A2), but I could have used ISO-8859-1 when generating the CharsetDecoder object (and thus
	 * only one byte, A2, in the ByteBuffer) and it would all have still worked out fine. Mixing up
	 * the two would have made it all go horribly wrong, though.
	 */
	private String getData( String charset ) throws Exception {
		byte[] bytes = new byte[0];

		byte[] cent = new byte[]{ (byte) 0xC2, (byte) 0xA2 };
		bytes = (byte[]) ArrayUtils.addAll( bytes, cent );

		// Add a space to make it _slightly_ easier to read the raw data, as you'll be looking out for a byte numbered '20'
		byte[] space = new byte[]{ (byte) 0x20 };
		bytes = (byte[]) ArrayUtils.addAll( bytes, space );

		ByteBuffer byteBuffer = ByteBuffer.wrap( bytes );

		CharsetDecoder decoder = Charset.forName( "UTF-8" ).newDecoder();
		CharBuffer charBuffer = decoder.decode( byteBuffer );
		StringBuffer xml = new StringBuffer( "<?xml version=\"1.0\" encoding=\"" + charset + "\" ?>\n" );
		xml.append( "<Request>\n" );
		xml.append( "<first>Hello</first>\n" );
		xml.append( "<second>" + charBuffer + "</second>\n" );
		xml.append( "</Request>\n" );

		return xml.toString();
	}

	/**
	 * A helper method which just dumps the file in the character set specified, typically so the user
	 * can then run 'od -x' on the file to see the raw bytes written to disk
	 * 
	 * @param text
	 * @param charset
	 * @throws Exception
	 */
	private void writeToFile( String text, String charset ) throws Exception {
		StringBuffer filename = new StringBuffer( "xmlParsing-" );
		filename.append( (charset.equals( "UTF-8" )) ? "utf8" : "latin1" );
		filename.append( ".txt" );
		FileOutputStream fos = new FileOutputStream( filename.toString() );
		fos.write( text.getBytes( charset ) );
		fos.close();
	}

	/**
	 * Convenience method to System.out.println()
	 * 
	 * @param text
	 */
	private void log( String text ) {
		System.out.println( text );
	}

}
