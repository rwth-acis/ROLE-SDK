login_body(param, static, bundle, literal, include) ::= <<
<div>$literal.error$</div>

<p>How would you like to sign in?</p>

<div style="margin: 1em; padding: .5em; border: 1px solid #cccccc; border-top: 3px solid #00cc00; cursor: pointer;" onclick="window.location.href='../openid/request?openid=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fid&amp;return=$param.return$';">
	<div><strong><a href="../openid/request?openid=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fid&amp;return=$param.return$"><img src="$static.openid.provider.google$" style="vertical-align: middle; padding: .5em;"/>Google</a>:</strong> sign in with my Google account</div>
</div>

<div style="margin: 1em; padding: .5em; border: 1px solid #cccccc; border-top: 3px solid #00cc00;">
	<div><strong><img src="$static.logo.small$" style="vertical-align: middle; padding: .5em;"/>ROLE:</strong> sign in with my ROLE account</div>
	<form action="login" method="post" style="padding-left: 2em; padding-bottom: 1em;">
		<table>
			<tr><th>$bundle.session.login.user$</th><td><input name="username" value=""/></td></tr>
			<tr><th>$bundle.session.login.password$</th><td><input name="password" type="password" value=""/></td></tr>
		</table>
		<div><input type="submit" value="$bundle.session.login.title$"/><input name="return" type="hidden" value="$param.return$"/></div>
	</form>
</div>
>>