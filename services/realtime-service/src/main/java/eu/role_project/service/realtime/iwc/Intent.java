package eu.role_project.service.realtime.iwc;

import java.net.URI;
import java.net.URISyntaxException;

public class Intent {
	
	//public static final String ROLE_IWC_XMLNS = "http://role-project.eu/iwc/intent";
	public static final String ROLE_IWC_XMLNS = "http://dbis.rwth-aachen.de/~hocken/da/xsd/Intent";
	
	private URI component;
	private URI sender;
	private String action;
	private String data;
	private String mimeType;
	
	private String[] categories;
	private String[] flags;
	private String extras;
	
	public Intent(String component, 
			String sender, 
			String action, 
			String data, 
			String mimeType,
			String[] categories,
			String[] flags,
			String extras
			){
		
		if(component == null){
			throw new IllegalArgumentException("Intent component not specified!");
		}
		try {
			this.component = new URI(component);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Intent component " + component + " is not a valid URI!");
		}
		
		if(sender == null){
			throw new IllegalArgumentException("Intent sender not specified!");
		}
		try {
			this.sender = new URI(sender);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Intent sender " + sender + " is not a valid URI!");
		}
		
		if(action == null){
			throw new IllegalArgumentException("Intent action not specified!");
		}
		this.action = action;
		
		this.data = data;
		this.mimeType = mimeType;
		this.categories = categories;
		this.flags = flags;
		this.extras = extras;
		
	}
	
	public String toXml(){
		String sPayload = "<intent xmlns='" + ROLE_IWC_XMLNS + "'>";
		sPayload += "<component>" + this.component.toString() + "</component>";
		sPayload +=	"<sender>" + this.sender + "</sender>"; 
		sPayload +=	"<action>" + this.action + "</action>"; 
		
		if(this.mimeType != null && this.data != null){
		sPayload += "<data mime='" + this.mimeType + "'>" + this.data + "</data>";  
		}
		
		if(this.categories != null && this.categories.length > 0){
			sPayload += "<categories>";
			for(int i=0;i<this.categories.length;i++){
				sPayload += "<category>" + this.categories[i] + "</category>";
			}
			sPayload +="</categories>";
		}
		
		if(this.flags != null && this.flags.length > 0){
			sPayload += "<flags>";
			for(int i=0;i<this.flags.length;i++){
				sPayload += "<flag>" + this.flags[i] + "</flag>";
			}
			sPayload +="</flags>";
		}
		
		if(extras != null && extras.length() > 0){
			sPayload += "<extras>" + extras + "</extras>";
		}
		else{
			sPayload += "<extras/>";
		}
			
	    sPayload += "</intent>";
		
	    System.out.println(sPayload);
		return sPayload;
	}
}
