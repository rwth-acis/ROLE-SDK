/**
 * Project: richContentMediaSearchService
 * ROLE-Project
 * authors: daniel.dahrendorf@im-c.de, julian.weber@im-c.de
 * This software uses the GNU GPL	
 */
package de.imc.advancedMediaSearch.lom;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.vcard4j.java.Type;
import net.sf.vcard4j.java.VCard;
import net.sf.vcard4j.java.type.FN;
import net.sf.vcard4j.parser.DomParser;
import net.sf.vcard4j.parser.VCardParseException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.imc.advancedMediaSearch.restlet.resource.ThumbRatingResource;
import de.imc.advancedMediaSearch.result.ResultEntity;
import de.imc.advancedMediaSearch.result.ResultTag;
import de.imc.advancedMediaSearch.result.ResultThumbnail;
import de.imc.advancedMediaSearch.result.ResultUser;
import de.imc.advancedMediaSearch.target.OpenScoutRepositoryTarget;

/**
 * @author julian.weber@im-c.de
 * 
 */
public class LomHelper {

	private static Logger logger = Logger.getLogger(LomHelper.class);

	public static ResultEntity parseLomGeneralNode(Node n, ResultEntity en,
			String repoid) {
		NodeList subnodes = n.getChildNodes();
		for (int i = 0; i < subnodes.getLength(); i++) {
			Node curNode = subnodes.item(i);

			// title
			if (curNode.getNodeName().endsWith("title")) {
				en.setTitle(extractStringValueFromLomNode(curNode));
			}
			// lang
			else if (curNode.getNodeName().endsWith("language")) {
				en.addLanguage(curNode.getFirstChild().getNodeValue());
			}
			// descr
			else if (curNode.getNodeName().endsWith("description")) {
				en.setDescription(extractStringValueFromLomNode(curNode));
			}
			// tags
			else if (curNode.getNodeName().endsWith("keyword")) {
				en.addTag(new ResultTag(extractStringValueFromLomNode(curNode),
						repoid));
			}

			// //openscout thumbnail
			// if(isOpenScout) {
			// String catalog = null;
			// String idstring = null;
			//
			// if(curNode.getNodeName().endsWith("identifier")) {
			// for(int count=0; count<curNode.getChildNodes().getLength();
			// count++) {
			// Node curSubN = curNode.getChildNodes().item(count);
			// if(curSubN.getNodeName().endsWith("catalog")) {
			// catalog = curSubN.getFirstChild().getNodeValue();
			// }
			// else if(curSubN.getNodeName().endsWith("entry")) {
			// idstring = curSubN.getFirstChild().getNodeValue();
			// }
			// }
			//
			// String url =
			// OpenScoutRepositoryTarget.generateOpenscoutThumbnail(catalog,idstring);
			//
			// if(url!=null) {
			// try {
			// URL tnurl = new URL(url);
			// ResultThumbnail tn = new ResultThumbnail();
			// tn.setUrl(tnurl);
			// en.setThumbnail(tn);
			// } catch (Exception e) {
			// logger.debug("An error occured while generating an openscout thumbnail url: "
			// + e.getMessage());
			// }
			// }
			// }
			// }
		}
		return en;
	}

	public static ResultEntity parseLomLifecycleNode(Node n, ResultEntity en,
			String repoid) {
		NodeList subnodes = n.getChildNodes();
		for (int i = 0; i < subnodes.getLength(); i++) {
			Node curNode = subnodes.item(i);

			// iterate through contribute subnodes to collect the author
			if (curNode.getNodeName().endsWith("contribute")) {
				NodeList nodes = curNode.getChildNodes();
				String userrole = null;

				for (int j = 0; j < nodes.getLength(); j++) {
					Node curSubNode = nodes.item(j);

					// get user role
					if (curSubNode.getNodeName().toLowerCase().endsWith("role")) {
						NodeList l = curSubNode.getChildNodes();

						for (int p = 0; p < l.getLength(); p++) {
							Node c = l.item(p);
							if (c.getNodeName().toLowerCase().endsWith("value")) {
								if (c.getFirstChild() != null) {
									userrole = c.getFirstChild().getNodeValue();
								}
							}
						}

					}
					// get user name
					// TODO: parse vcard email address here
					else if (curSubNode.getNodeName().toLowerCase()
							.endsWith("entity")) {
						if (curSubNode.getFirstChild() != null) {
							String username = parseVCardName(curSubNode
									.getFirstChild().getNodeValue());
							ResultUser u = new ResultUser(username, null, null,
									null, repoid);

							if (userrole != null
									&& userrole.toLowerCase().equals(
											"publisher")) {
								en.setUploader(u);
							} else {
								en.addAuthor(u);
							}
						}
					}
				}
			}
		}
		return en;
	}

	public static ResultEntity parseLomMetaMetadataNode(Node n,
			ResultEntity en, String repoid) {
		// TODO: implement lom metametadata parsing function
		NodeList subnodes = n.getChildNodes();
		for (int i = 0; i < subnodes.getLength(); i++) {
			Node curNode = subnodes.item(i);

			// iterate through contribute subnodes to collect authors
			if (curNode.getNodeName().endsWith("contribute")) {
				NodeList nodes = curNode.getChildNodes();
				String userrole = null;

				for (int j = 0; j < nodes.getLength(); j++) {
					Node curSubNode = nodes.item(j);

					// get user role
					if (curSubNode.getNodeName().toLowerCase().endsWith("role")) {
						NodeList l = curSubNode.getChildNodes();

						for (int p = 0; p < l.getLength(); p++) {
							Node c = l.item(p);
							if (c.getNodeName().toLowerCase().endsWith("value")) {
								if (c.getFirstChild() != null) {
									userrole = c.getFirstChild().getNodeValue();
								}
							}
						}

					}
					// get user name
					// TODO: parse vcard email address here
					else if (curSubNode.getNodeName().toLowerCase()
							.endsWith("entity")) {
						if (curSubNode.getFirstChild() != null) {
							String username = parseVCardName(curSubNode
									.getFirstChild().getNodeValue());
							ResultUser u = new ResultUser(username, null, null,
									null, repoid);

							if (userrole != null
									&& userrole.toLowerCase().equals(
											"publisher")) {
								en.setUploader(u);
							} else {
								en.addAuthor(u);
							}
						}
					}
				}
			}
		}
		return en;
	}

	public static ResultEntity parseLomTechnicalNode(Node n, ResultEntity en) {
		if (n != null) {
			NodeList subnodes = n.getChildNodes();
			if (subnodes != null) {
				for (int i = 0; i < subnodes.getLength(); i++) {

					Node curNode = subnodes.item(i);

					if (curNode != null) {
						// format
						if (curNode.getNodeName().endsWith("format")) {
							if (curNode.getFirstChild() != null) {
								en.setFormat(curNode.getFirstChild()
										.getNodeValue());
							}
						}

						// url
						if (curNode.getNodeName().endsWith("location")) {
							if (curNode.getFirstChild() != null) {
								en.setUrl(curNode.getFirstChild()
										.getNodeValue());
							}
						}
					}
				}
			}
		}
		en.updateMediaType();
		return en;
	}

	private static String extractStringValueFromLomNode(Node n) {
		String ret = "";
		NodeList nodes = n.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			Node curNode = nodes.item(i);
			if (curNode.getNodeName().endsWith("string")) {
				if (curNode.getFirstChild() != null) {
					ret = curNode.getFirstChild().getNodeValue();
					break;
				}
			}
		}
		return ret;
	}

	private static String parseVCardName(String vCardString) {

		try {
			String name = "";

			DomParser parser = new DomParser();

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder;
			try {
				documentBuilder = documentBuilderFactory.newDocumentBuilder();

				Document document = documentBuilder.newDocument();

				Reader reader = new StringReader(vCardString);

				parser.parse(reader, document);

				VCard vCard = new VCard((Element) document.getDocumentElement()
						.getElementsByTagName("vcard").item(0));

				@SuppressWarnings("unchecked")
				Iterator<Type> iter = vCard.getTypes();

				while (iter.hasNext()) {
					Type tmpType = iter.next();

					if (tmpType.getName().equalsIgnoreCase("FN")) {
						name = ((FN) tmpType).get();
					}
				}
			} catch (ParserConfigurationException e) {
				name = vCardString;

				logger.error(e.toString() + " :" + vCardString);
			} catch (VCardParseException e) {
				name = vCardString;

				logger.error(e.toString() + " :" + vCardString);
			} catch (IOException e) {
				name = vCardString;

				logger.error(e.toString() + " :" + vCardString);
			}

			return name;

		} catch (Exception e) {
			return "";
		}
	}
}
