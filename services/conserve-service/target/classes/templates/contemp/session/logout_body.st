logout_body(param, static, bundle, literal, include) ::= <<
<div>$literal.error$</div>

<form action="logout" method="post">
	<input type="submit" value="$bundle.session.logout.title$"/><input name="return" type="hidden" value="$param.return$"/>
</form>
>>