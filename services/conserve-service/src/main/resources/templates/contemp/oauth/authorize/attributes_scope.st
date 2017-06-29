attributes_scope(param, static, bundle, literal, include) ::= <<
$bundle.oauth_attributes.the_requested_scope$

<ul>
$param.attributes.(attribute):{scope|
	<li><strong>$bundle.oauth_attributes.scope.(scope).title$</strong> (<em>$scope$</em>)</li>
}$
</ul>
>>