package eu.role_project.service.realtime.xmpp;

import java.util.Collections;
import java.util.List;

import org.jivesoftware.smackx.pubsub.NodeExtension;
import org.jivesoftware.smackx.pubsub.PubSubElementType;

public class OwnerAffiliationsExtension extends NodeExtension
{
	protected List<OwnerAffiliation> items = Collections.EMPTY_LIST;
	
	public OwnerAffiliationsExtension(String nodeId)
	{
		super(PubSubElementType.AFFILIATIONS,nodeId);
	}
	
	public OwnerAffiliationsExtension(String nodeId, List<OwnerAffiliation> subList)
	{
		super(PubSubElementType.AFFILIATIONS, nodeId);
		items = subList;
	}

	public List<OwnerAffiliation> getAffiliations()
	{
		return items;
	}

	@Override
	public String toXML()
	{
		if ((items == null) || (items.size() == 0))
		{
			return super.toXML();
		}
		else
		{
			StringBuilder builder = new StringBuilder("<");
			builder.append(getElementName());
			builder.append(" node='" + getNode());
			builder.append("'>");
			
			for (OwnerAffiliation item : items)
			{
				builder.append(item.toXML());
			}
			
			builder.append("</");
			builder.append(getElementName());
			builder.append(">");
			return builder.toString();
		}
	}
}
