authorize_body(param, static, bundle, literal, include) ::= <<
<p>$bundle.openid.authorize.the_service$</p>
<blockquote><strong>$param.realm$</strong></blockquote>

<p>$bundle.openid.authorize.wants_authorization$</p>

<hr/>

<form action="authorized" method="post">
$param.form.keys:{key|
	<input type="hidden" name="$key$" value="$param.form.(key)$"/>
}$
	<input type="submit" value="Authorize"/>
</form>
>>