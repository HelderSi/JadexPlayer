package jadex.adapter.fipa;

import jadex.runtime.NuggetsXMLContentCodec;
import jadex.util.collection.SCollection;

import java.util.Collection;
import java.util.Set;

/**
 *  Helper class for JADE specific issues.
 */
public class SFipa
{
	//-------- constants --------

	/** The FIPA message type. */
	public static final String MESSAGE_TYPE_NAME_FIPA = "fipa";

	/** The FIPA AMS local agent identifier. */
	public static final AgentIdentifier AMS = new AgentIdentifier("ams", true);

	/** The FIPA DF local agent identifier. */
	public static final AgentIdentifier DF = new AgentIdentifier("df", true);

	// Protocols
	public static final String	PROTOCOL_REQUEST	= "fipa-request";
	public static final String	PROTOCOL_QUERY	= "fipa-query" ;
	public static final String	PROTOCOL_REQUEST_WHEN	= "fipa-request-when";
	public static final String	PROTOCOL_BROKERING	= "fipa-brokering";
	public static final String	PROTOCOL_RECRUITING	= "fipa-recruiting";
	public static final String	PROTOCOL_PROPOSE	= "fipa-propose";
	public static final String	PROTOCOL_SUBSCRIBE	= "fipa-subscribe";
	public static final String	PROTOCOL_ENGLISH_AUCTION	= "fipa-auction-english";
	public static final String	PROTOCOL_DUTCH_AUCTION	= "fipa-auction-dutch";
	public static final String	PROTOCOL_CONTRACT_NET	= "fipa-contract-net";  
	public static final String	PROTOCOL_ITERATED_CONTRACT_NET	= "fipa-iterated-contract-net";
	public static final String	PROTOCOL_CANCEL_META	= "fipa-cancel-meta";
	
	/** The standard protocols. */
	public static Collection PROTOCOLS;

	static
	{
		// cannot use hashset, as order is important for gui usage.
		PROTOCOLS	= SCollection.createArrayList();
		PROTOCOLS.add(PROTOCOL_REQUEST);
		PROTOCOLS.add(PROTOCOL_QUERY);
		PROTOCOLS.add(PROTOCOL_REQUEST_WHEN);
		PROTOCOLS.add(PROTOCOL_BROKERING);
		PROTOCOLS.add(PROTOCOL_RECRUITING);
		PROTOCOLS.add(PROTOCOL_PROPOSE);
		PROTOCOLS.add(PROTOCOL_SUBSCRIBE);
		PROTOCOLS.add(PROTOCOL_ENGLISH_AUCTION);
		PROTOCOLS.add(PROTOCOL_DUTCH_AUCTION);
		PROTOCOLS.add(PROTOCOL_CONTRACT_NET);
		PROTOCOLS.add(PROTOCOL_ITERATED_CONTRACT_NET);
		PROTOCOLS.add(PROTOCOL_CANCEL_META);
	}

	// Performatives.
	public static final String ACCEPT_PROPOSAL = "accept-proposal";
	public static final String AGREE = "agree";
	public static final String CANCEL = "cancel";
	public static final String CFP = "cfp";
	public static final String CONFIRM = "confirm";
	public static final String DISCONFIRM = "disconfirm";
	public static final String FAILURE = "failure";
	public static final String INFORM = "inform";
	public static final String INFORM_IF = "inform-if";
	public static final String INFORM_REF = "inform-ref";
	public static final String NOT_UNDERSTOOD = "not-understood";
	public static final String PROPOSE = "propose";
	public static final String QUERY_IF = "query-if";
	public static final String QUERY_REF = "query-ref";
	public static final String REFUSE = "refuse";
	public static final String REJECT_PROPOSAL = "reject-proposal";
	public static final String REQUEST = "request";
	public static final String REQUEST_WHEN = "request-when";
	public static final String REQUEST_WHENEVER = "request-whenever";
	public static final String SUBSCRIBE = "subscribe";
	public static final String PROXY = "proxy";
	public static final String PROPAGATE = "propagate";
	public static final String UNKNOWN = "unknown";

	/** The allowed message attributes. */
	public static Collection PERFORMATIVES;

	static
	{
		// cannot use hashset, as order is important for gui usage.
		PERFORMATIVES = SCollection.createArrayList();
		PERFORMATIVES.add(ACCEPT_PROPOSAL);
		PERFORMATIVES.add(AGREE);
		PERFORMATIVES.add(CANCEL);
		PERFORMATIVES.add(CFP);
		PERFORMATIVES.add(CONFIRM);
		PERFORMATIVES.add(DISCONFIRM);
		PERFORMATIVES.add(FAILURE);
		PERFORMATIVES.add(INFORM);
		PERFORMATIVES.add(INFORM_IF);
		PERFORMATIVES.add(INFORM_REF);
		PERFORMATIVES.add(NOT_UNDERSTOOD);
		PERFORMATIVES.add(PROPOSE);
		PERFORMATIVES.add(QUERY_IF);
		PERFORMATIVES.add(QUERY_REF);
		PERFORMATIVES.add(REFUSE);
		PERFORMATIVES.add(REJECT_PROPOSAL);
		PERFORMATIVES.add(REQUEST);
		PERFORMATIVES.add(REQUEST_WHEN);
		PERFORMATIVES.add(REQUEST_WHENEVER);
		PERFORMATIVES.add(SUBSCRIBE);
		PERFORMATIVES.add(PROXY);
		PERFORMATIVES.add(PROPAGATE);
		PERFORMATIVES.add(UNKNOWN);
	}

	// Names of the various fields of an ACL messages.
	public static final String ENCODING = "encoding";
	public static final String IN_REPLY_TO = "in-reply-to";
	public static final String LANGUAGE = "language";
	public static final String ONTOLOGY = "ontology";
	public static final String PROTOCOL = "protocol";
	public static final String REPLY_BY = "reply-by";
	public static final String REPLY_WITH = "reply-with";
	/** Not FIPA compliant, should be "receiver". */
	public static final String RECEIVERS = "receivers";
	public static final String REPLY_TO = "reply-to";
	public static final String PERFORMATIVE = "performative";
	public static final String CONTENT = "content";
	public static final String SENDER = "sender";
//	public static final String REPLY_BY_DATE = "reply-by-date";
	public static final String CONTENT_START = "content-start";
	public static final String CONTENT_CLASS = "content-class";
	public static final String ACTION_CLASS = "action-class";
	public static final String CONVERSATION_ID = "conversation-id";

	public static final String X_MESSAGE_ID = "x-message-id";


	/** The allowed message attributes. */
	public static Set MESSAGE_ATTRIBUTES;

	static
	{
		MESSAGE_ATTRIBUTES = SCollection.createHashSet();
		MESSAGE_ATTRIBUTES.add(CONVERSATION_ID);
		MESSAGE_ATTRIBUTES.add(ENCODING);
		MESSAGE_ATTRIBUTES.add(IN_REPLY_TO);
		MESSAGE_ATTRIBUTES.add(LANGUAGE);
		MESSAGE_ATTRIBUTES.add(ONTOLOGY);
		MESSAGE_ATTRIBUTES.add(PROTOCOL);
		MESSAGE_ATTRIBUTES.add(REPLY_BY);
		MESSAGE_ATTRIBUTES.add(REPLY_WITH);
		MESSAGE_ATTRIBUTES.add(RECEIVERS);
		MESSAGE_ATTRIBUTES.add(REPLY_TO);
		MESSAGE_ATTRIBUTES.add(PERFORMATIVE);
		MESSAGE_ATTRIBUTES.add(CONTENT);
		MESSAGE_ATTRIBUTES.add(SENDER);
//		MESSAGE_ATTRIBUTES.add(REPLY_BY_DATE);
		MESSAGE_ATTRIBUTES.add(CONTENT_START);
		MESSAGE_ATTRIBUTES.add(CONTENT_CLASS);
		MESSAGE_ATTRIBUTES.add(ACTION_CLASS);		
	}

	// Content languages.
	public static final String FIPA_SL0 = "fipa-sl0";
	public static final String FIPA_SL1 = "fipa-sl1";
	public static final String FIPA_SL2 = "fipa-sl2";
	public static final String FIPA_SL  = "fipa-sl";
	public static final String JAVA_XML = "java-xml"; // JavaXMLContentCodec.JAVA_XML; // Hack!!! avoid dependency for 1.3 compliance
	public static final String NUGGETS_XML = NuggetsXMLContentCodec.NUGGETS_XML;
	//public static final String XSTREAM_XML = "xstream-xml"; //todo

	/**
	 *  Create a service description.
	 *  @param name	The service name.
	 *  @param type	The service type.
	 *  @param ownership	The ownership of the service.
	 */
	public static ServiceDescription	createServiceDescription(String name, String type, String ownership)
	{
		return new ServiceDescription(name, type, ownership);
	}

	/**
	 *  Create a service description.
	 *  @param name	The service name.
	 *  @param type	The service type.
	 *  @param ownership	The ownership of the service.
	 *  @param languages	The languages understood by the service.
	 *  @param ontologies	The ontologies known by the service.
	 *  @param protocols	The protocols used by the service.
	 *  @param properties	Any additional service properties.
	 */
	public static ServiceDescription	createServiceDescription(String name, String type, String ownership,
		String[] languages, String[] ontologies, String[] protocols, Property[] properties)
	{
		ServiceDescription	ret	= new ServiceDescription(name, type, ownership);
		for(int i=0; languages!=null && i<languages.length; i++)
			ret.addLanguage(languages[i]);
		for(int i=0; ontologies!=null && i<ontologies.length; i++)
			ret.addOntology(ontologies[i]);
		for(int i=0; protocols!=null && i<protocols.length; i++)
			ret.addProtocol(protocols[i]);
		for(int i=0; properties!=null && i<properties.length; i++)
			ret.addProperty(properties[i]);
		return ret;
	}

	/**
	 *  Create an agent description.
	 *  @param agent	The agent id.
	 *  @param service	The agent service.
	 */
	public static AgentDescription	createAgentDescription(AgentIdentifier agent, ServiceDescription service)
	{
		AgentDescription	ret	= new AgentDescription();
		ret.setName(agent);
		if(service!=null)
			ret.addService(service);
		return ret;
	}

	/**
	 *  Create an agent description.
	 *  @param agent	The agent id.
	 *  @param services	The agent service.
	 *  @param languages	The languages understood by the service.
	 *  @param ontologies	The ontologies known by the service.
	 *  @param protocols	The protocols used by the service.
	 */
	public static AgentDescription	createAgentDescription(AgentIdentifier agent, ServiceDescription[] services,
		String[] languages, String[] ontologies, String[] protocols)
	{
		AgentDescription	ret	= new AgentDescription();
		ret.setName(agent);
		for(int i=0; services!=null && i<services.length; i++)
			ret.addService(services[i]);
		for(int i=0; languages!=null && i<languages.length; i++)
			ret.addLanguage(languages[i]);
		for(int i=0; ontologies!=null && i<ontologies.length; i++)
			ret.addOntology(ontologies[i]);
		for(int i=0; protocols!=null && i<protocols.length; i++)
			ret.addProtocol(protocols[i]);
		return ret;
	}

	/** The counter for conversation ids. */
	protected static int	convidcnt;

	/**
	 *  Create a globally unique conversation id.
	 *  @return The conversation id.
	 */
	public static String createUniqueId(String name)
	{
		return "id_"+name+"_"+System.currentTimeMillis()+"_"+Math.random()+"_"+(++convidcnt);
	}
}