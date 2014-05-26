<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:OAuthEndpointSetup="xalan://eu.role_project.service.resource.OAuthEndpointSetup"
	xmlns:GadgetHTMLProxy="xalan://eu.role_project.service.resource.GadgetHTMLProxy"
	extension-element-prefixes="OAuthEndpointSetup GadgetHTMLProxy"
	xmlns:openapp="http://www.role-project.eu/xml/openapp/opensocialext/"
	exclude-result-prefixes="openapp">

	<xsl:param name="endpointSetup" />
	<xsl:param name="htmlProxy" />
	<xsl:param name="baseUri" />
	<xsl:param name="gadgetBaseUri" />

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/Module/ModulePrefs/OAuth/Service[@openapp:service]">
		<Service name="{@name}">
			<Request
				url="{OAuthEndpointSetup:getRequestUrl($endpointSetup, current(), Request/@url)}"
				method="{OAuthEndpointSetup:getRequestMethod($endpointSetup, current(), Request/@method)}" />
			<Authorization
				url="{OAuthEndpointSetup:getAuthorizationUrl($endpointSetup, current(), Authorization/@url)}" />
			<Access
				url="{OAuthEndpointSetup:getAccessUrl($endpointSetup, current(), Access/@url)}"
				method="{OAuthEndpointSetup:getAccessMethod($endpointSetup, current(), Access/@method)}" />
		</Service>
	</xsl:template>

	<xsl:template match="Content[@type='html']">
		<xsl:copy>
			<xsl:apply-templates select="@*" />
			<xsl:if
				test="/Module/ModulePrefs/Optional[@feature='openapp'] | /Module/ModulePrefs/Require[@feature='openapp']">
    <![CDATA[<script src="]]><xsl:value-of select="$baseUri" /><![CDATA[d/openapp"></script>]]></xsl:if>
			<xsl:choose>
				<xsl:when
					test="/Module/ModulePrefs/Optional[@feature='openapp']/Param[@name='gadget-base-rewrite']">
					<xsl:value-of
						select="GadgetHTMLProxy:replace($htmlProxy, text(), /Module/ModulePrefs/Optional[@feature='openapp']/Param[@name='gadget-base-rewrite']/text(), $gadgetBaseUri)" />
				</xsl:when>
				<xsl:when
					test="/Module/ModulePrefs/Require[@feature='openapp']/Param[@name='gadget-base-rewrite']">
					<xsl:value-of
						select="GadgetHTMLProxy:replace($htmlProxy, text(), /Module/ModulePrefs/Require[@feature='openapp']/Param[@name='gadget-base-rewrite']/text(), $gadgetBaseUri)" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of
						select="GadgetHTMLProxy:replace($htmlProxy, text(), 'http://gadget.base.role-project.eu/', $gadgetBaseUri)" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/Module/ModulePrefs/Optional[@feature='openapp']" />
	<xsl:template match="/Module/ModulePrefs/Require[@feature='openapp']" />

</xsl:stylesheet>