redirect(param, static, bundle, literal, include) ::= <<
<!DOCTYPE HTML>
<html>
	<head>
		<title>$literal.title$</title>
		<meta http-equiv="Refresh" content="1; url=$param.redirect$" />
	</head>
	<body>
		<p>$literal.message$ $literal.message2$</p>
	</body>
</html>
>>