authorize_body(param, static, bundle, literal, include) ::= <<
<p>$bundle.oauth.authorize.the_service$</p>
<blockquote><strong>$param.consumer_key$</strong></blockquote>

<p>$bundle.oauth.authorize.wants_authorization$</p>
<blockquote><strong>$param.resource_owner$</strong>.</blockquote>

$param.attributes.keys:{attribute|
<p>
	<strong>$literal.attributes.(attribute)$:</strong>
	$include.attributes.(attribute)$
</p>
}$

<hr/>

<form action="authorized" method="post">
	<input name="oauth_token" type="hidden" value="$param.oauth_token$"/>
	<input type="submit" value="Authorize"/>
</form>
>>